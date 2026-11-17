package com.asdflj.ae2thing.client.me;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;
import com.asdflj.ae2thing.network.CPacketCellContentsRequest;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class InfinityCellContentsCache {

    private static final long REQUEST_INTERVAL = 150L;
    private static final long REFRESH_INTERVAL = 30000L;
    private static final long ENTRY_EXPIRE = 120000L;
    private static final long PRUNE_INTERVAL = 30000L;
    private static final int MAX_ENTRIES = 128;

    private static final Object2ObjectMap<String, Object2ObjectMap<String, Entry>> CACHE = new Object2ObjectOpenHashMap<>();

    private static long version;
    private static long lastPrune;

    private InfinityCellContentsCache() {}

    public static long version() {
        return version;
    }

    public static void clear() {
        CACHE.clear();
    }

    public static List<IAEStack<?>> contents(ItemStack cell, IAEStackType<?> type) {
        final Entry entry = entry(cell, type, true);
        if (entry == null) return Collections.emptyList();
        entry.lastAccess = System.currentTimeMillis();
        if (entry.stale()) request(cell, type, entry, 0);
        return entry.stacks;
    }

    public static void requestMore(ItemStack cell, IAEStackType<?> type) {
        final Entry entry = entry(cell, type, false);
        if (entry == null || entry.complete || entry.stacks.isEmpty()) return;
        entry.lastAccess = System.currentTimeMillis();
        if (entry.stacks.size() >= InfinityStorageCellSnapshot.MAX_OFFSET) return;
        request(cell, type, entry, entry.stacks.size());
    }

    public static void receive(String uuid, String typeId, int offset, NBTTagCompound payload) {
        final IAEStackType<?> type = AEStackTypeRegistry.getType(typeId);
        if (uuid == null || uuid.isEmpty() || type == null) return;
        final Entry entry = entry(uuid, typeId, true);
        if (entry == null) return;
        if (offset != 0 && offset != entry.stacks.size()) return;
        final long now = System.currentTimeMillis();
        entry.lastAccess = now;
        final NBTTagList list = payload
            .getTagList(InfinityStorageCellSnapshot.NBT_PAGE_ENTRIES, Constants.NBT.TAG_COMPOUND);
        final int replace = offset == 0 ? Math.min(list.tagCount(), entry.stacks.size()) : 0;
        for (int i = 0; i < list.tagCount(); i++) {
            final IAEStack<?> stack = type.loadStackFromNBT(list.getCompoundTagAt(i));
            if (stack == null) continue;
            if (i < replace) entry.stacks.set(i, stack);
            else entry.stacks.add(stack);
        }
        entry.total = payload.getInteger(InfinityStorageCellSnapshot.NBT_PAGE_TOTAL);
        while (entry.stacks.size() > entry.total) {
            entry.stacks.removeLast();
        }
        entry.complete = entry.stacks.size() >= entry.total;
        version++;
        prune(now);
    }

    private static void prune(long now) {
        if (now - lastPrune < PRUNE_INTERVAL) return;
        lastPrune = now;
        int entries = 0;
        final Iterator<Object2ObjectMap.Entry<String, Object2ObjectMap<String, Entry>>> outer = CACHE
            .object2ObjectEntrySet()
            .iterator();
        while (outer.hasNext()) {
            final Object2ObjectMap<String, Entry> byType = outer.next()
                .getValue();
            final Iterator<Entry> inner = byType.values()
                .iterator();
            while (inner.hasNext()) {
                final Entry entry = inner.next();
                if (now - entry.lastAccess >= ENTRY_EXPIRE) inner.remove();
                else entries++;
            }
            if (byType.isEmpty()) outer.remove();
        }
        if (entries > MAX_ENTRIES) CACHE.clear();
    }

    private static void request(ItemStack cell, IAEStackType<?> type, Entry entry, int offset) {
        final long now = System.currentTimeMillis();
        if (now - entry.lastRequest < REQUEST_INTERVAL) return;
        final String uuid = uuid(cell);
        if (uuid.isEmpty()) return;
        entry.lastRequest = now;
        entry.lastRefresh = now;
        AE2Thing.proxy.netHandler.sendToServer(new CPacketCellContentsRequest(uuid, type.getId(), offset));
    }

    @Nullable
    private static Entry entry(ItemStack cell, IAEStackType<?> type, boolean create) {
        if (cell == null || type == null) return null;
        return entry(uuid(cell), type.getId(), create);
    }

    @Nullable
    private static Entry entry(String uuid, String typeId, boolean create) {
        if (uuid.isEmpty()) return null;
        Object2ObjectMap<String, Entry> byType = CACHE.get(uuid);
        if (byType == null) {
            if (!create) return null;
            byType = new Object2ObjectOpenHashMap<>();
            CACHE.put(uuid, byType);
        }
        Entry entry = byType.get(typeId);
        if (entry == null && create) {
            entry = new Entry();
            byType.put(typeId, entry);
        }
        return entry;
    }

    private static String uuid(ItemStack cell) {
        if (cell == null) return "";
        final String uuid = ItemStackNBT.getString(cell, com.asdflj.ae2thing.api.Constants.DISKUUID);
        return uuid == null ? "" : uuid;
    }

    private static final class Entry {

        private final List<IAEStack<?>> stacks = new ObjectArrayList<>();
        private int total;
        private boolean complete;
        private long lastRequest;
        private long lastRefresh;
        private long lastAccess = System.currentTimeMillis();

        private boolean stale() {
            return System.currentTimeMillis() - this.lastRefresh >= REFRESH_INTERVAL;
        }
    }
}
