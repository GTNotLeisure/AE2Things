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

    private static final long POLL_INTERVAL = 400L;
    private static final long RESPONSE_TIMEOUT = 2000L;
    private static final long ENTRY_EXPIRE = 120000L;
    private static final long PRUNE_INTERVAL = 30000L;
    private static final int MAX_ENTRIES = 128;
    private static final int PAGE_SIZE = InfinityStorageCellSnapshot.PAGE_SIZE;

    private static final Object2ObjectMap<String, Object2ObjectMap<String, Entry>> CACHE = new Object2ObjectOpenHashMap<>();

    private static long version;
    private static long lastPrune;

    private InfinityCellContentsCache() {}

    public static long version() {
        return version;
    }

    public static void clear() {
        CACHE.clear();
        version++;
    }

    // region client

    public static void poll(ItemStack cell, IAEStackType<?> type, int limit) {
        poll(cell, type, limit, POLL_INTERVAL);
    }

    public static void poll(ItemStack cell, IAEStackType<?> type, int limit, long interval) {
        final Entry e = entry(cell, type, true);
        if (e == null) return;
        final long now = System.currentTimeMillis();
        e.lastAccess = now;
        final int requested = Math.clamp(limit, 0, PAGE_SIZE);
        e.requestedLimit = Math.max(e.requestedLimit, requested);
        if (!canRequest(e, now, interval)) return;

        final String uuid = uuid(cell);
        if (uuid.isEmpty()) return;

        final int effectiveLimit = e.requestedLimit;
        final long requestRev = effectiveLimit > e.limit ? -1L : e.listRev;

        sendRequest(uuid, type.getId(), e, 0, effectiveLimit, requestRev, now);
    }

    public static List<IAEStack<?>> contents(ItemStack cell, IAEStackType<?> type) {
        final Entry entry = entry(cell, type, false);
        return entry == null ? Collections.emptyList() : entry.stacks;
    }

    @Nullable
    public static CellStats stats(ItemStack cell, IAEStackType<?> type) {
        final Entry entry = entry(cell, type, false);

        if (entry == null || entry.rev < 0) {
            return null;
        }

        return new CellStats(entry.total, entry.count, entry.rev);
    }

    @Nullable
    public static CellStats displayStats(ItemStack cell, IAEStackType<?> type) {
        final CellStats live = stats(cell, type);
        if (live != null) return live;
        final NBTTagCompound section = InfinityStorageCellSnapshot.section(cell, type);
        if (section == null) return null;
        return new CellStats(
            section.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES),
            section.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT),
            -1L);
    }

    public static void requestMore(ItemStack cell, IAEStackType<?> type) {
        final Entry entry = entry(cell, type, false);
        if (entry == null) return;

        final long now = System.currentTimeMillis();
        entry.lastAccess = now;
        if (entry.listRev < 0 || entry.listRev != entry.rev) {
            poll(cell, type, PAGE_SIZE);
            return;
        }

        if (entry.complete || entry.stacks.isEmpty()) return;
        if (entry.limit < PAGE_SIZE) return;
        if (entry.stacks.size() >= entry.total) return;
        if (entry.stacks.size() >= InfinityStorageCellSnapshot.MAX_OFFSET) return;

        if (!canRequest(entry, now, POLL_INTERVAL)) return;

        final String uuid = uuid(cell);
        if (uuid.isEmpty()) return;

        sendRequest(uuid, type.getId(), entry, entry.stacks.size(), PAGE_SIZE, entry.listRev, now);
    }

    // endregion

    public static void receive(String uuid, String typeId, int offset, NBTTagCompound payload) {
        if (uuid == null || uuid.isEmpty() || payload == null || offset < 0) {
            return;
        }

        final IAEStackType<?> type = AEStackTypeRegistry.getType(typeId);
        if (type == null) return;
        final Entry entry = entry(uuid, typeId, true);
        if (entry == null) return;

        final long now = System.currentTimeMillis();
        final int total = Math.max(0, payload.getInteger(InfinityStorageCellSnapshot.NBT_PAGE_TOTAL));
        final long count = payload.getLong(InfinityStorageCellSnapshot.NBT_PAGE_COUNT);
        final long replyRev = payload.getLong(InfinityStorageCellSnapshot.NBT_PAGE_REV);
        final int replyLimit = Math.clamp(payload.getInteger(InfinityStorageCellSnapshot.NBT_PAGE_LIMIT), 0, PAGE_SIZE);
        final NBTTagList list = payload.hasKey(InfinityStorageCellSnapshot.NBT_PAGE_ENTRIES, Constants.NBT.TAG_LIST)
            ? payload.getTagList(InfinityStorageCellSnapshot.NBT_PAGE_ENTRIES, Constants.NBT.TAG_COMPOUND)
            : null;
        entry.lastAccess = now;
        entry.pending = false;
        if (offset > 0
            && (list == null || replyLimit == 0 || entry.listRev != replyRev || offset != entry.stacks.size())) {
            prune(now);
            return;
        }

        boolean changed = false;
        entry.total = total;
        entry.count = count;
        entry.rev = replyRev;
        if (entry.listRev >= 0 && entry.listRev != replyRev) {
            entry.stacks.clear();
            entry.listRev = -1;
            entry.limit = 0;
            changed = true;
        }

        if (list != null && replyLimit > 0) {
            final List<IAEStack<?>> decoded = new ObjectArrayList<>();
            for (int i = 0; i < list.tagCount(); i++) {
                final IAEStack<?> stack = type.loadStackFromNBT(list.getCompoundTagAt(i));

                if (stack != null) {
                    decoded.add(stack);
                }
            }
            if (offset == 0) {
                if (entry.listRev != replyRev || replyLimit >= entry.limit) {
                    entry.stacks.clear();
                    entry.stacks.addAll(decoded);

                    entry.limit = replyLimit;
                    entry.listRev = replyRev;
                    entry.requestedLimit = Math.max(entry.requestedLimit, replyLimit);
                }
            } else {
                entry.stacks.addAll(decoded);
            }
            changed = true;
        }
        while (entry.stacks.size() > entry.total) {
            entry.stacks.removeLast();
            changed = true;
        }
        entry.complete = entry.listRev == entry.rev && entry.stacks.size() >= entry.total;
        if (changed) version++;
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean canRequest(Entry entry, long now, long interval) {
        if (entry.pending && now - entry.lastRequest < RESPONSE_TIMEOUT) {
            return false;
        }

        return now - entry.lastRequest >= Math.max(POLL_INTERVAL, interval);
    }

    private static void sendRequest(String uuid, String typeId, Entry entry, int offset, int limit, long rev,
        long now) {

        entry.pending = true;
        entry.lastRequest = now;

        AE2Thing.proxy.netHandler.sendToServer(new CPacketCellContentsRequest(uuid, typeId, offset, limit, rev));
    }

    public record CellStats(long types, long count, long rev) {}

    private static final class Entry {

        private final List<IAEStack<?>> stacks = new ObjectArrayList<>();
        private int total;
        private long count;
        private long rev = -1;
        private long listRev = -1;
        private int limit;
        private int requestedLimit;
        private boolean complete;
        private boolean pending;
        private long lastRequest;
        private long lastAccess = System.currentTimeMillis();

    }
}
