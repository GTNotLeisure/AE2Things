package com.asdflj.ae2thing.common.storage.infinityCell;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.DataStorage;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

public final class InfinityStorageCellSnapshot {

    public static final String NBT_DATA = "ucData";
    public static final String NBT_SNAPSHOT_TYPES = "types";
    public static final String NBT_SNAPSHOT_COUNT = "count";
    public static final String NBT_PAGE_TOTAL = "total";
    public static final String NBT_PAGE_ENTRIES = "entries";

    public static final int PAGE_SIZE = 256;
    public static final int MAX_OFFSET = PAGE_SIZE * 32;

    private static final long FLUSH_INTERVAL = 2000L;

    private static final Object2ObjectMap<UUID, Object2ObjectMap<String, Entry>> ENTRIES = new Object2ObjectOpenHashMap<>();

    private InfinityStorageCellSnapshot() {}

    public static void markDirty(DataStorage storage, IAEStackType<?> type, ItemStack cell) {
        if (Platform.isClient() || storage == null || type == null || cell == null) return;
        final Entry entry = entry(storage, type);
        if (entry.cell.get() != cell) entry.cell = new WeakReference<>(cell);
        entry.dirty = true;
    }

    public static void markDirtyIfMissing(DataStorage storage, IAEStackType<?> type, ItemStack cell) {
        if (storage == null || section(cell, type) != null) return;
        markDirty(storage, type, cell);
    }

    public static void flushAll() {
        if (ENTRIES.isEmpty()) return;
        final long now = System.currentTimeMillis();
        final Iterator<Object2ObjectMap.Entry<UUID, Object2ObjectMap<String, Entry>>> outer = ENTRIES
            .object2ObjectEntrySet()
            .iterator();
        while (outer.hasNext()) {
            final Object2ObjectMap<String, Entry> byType = outer.next()
                .getValue();
            final Iterator<Entry> inner = byType.values()
                .iterator();
            while (inner.hasNext()) {
                final Entry entry = inner.next();
                if (entry.cell.get() == null) {
                    inner.remove();
                    continue;
                }
                if (!entry.dirty || now - entry.lastWrite < FLUSH_INTERVAL) continue;
                write(entry);
            }
            if (byType.isEmpty()) outer.remove();
        }
    }

    public static void flushAllForce() {
        if (ENTRIES.isEmpty()) return;
        for (final Object2ObjectMap<String, Entry> byType : ENTRIES.values()) {
            for (final Entry entry : byType.values()) {
                if (entry.dirty) write(entry);
            }
        }
    }

    public static void flushNow(ItemStack cell) {
        if (ENTRIES.isEmpty() || cell == null) return;
        for (final Object2ObjectMap<String, Entry> byType : ENTRIES.values()) {
            for (final Entry entry : byType.values()) {
                if (entry.dirty && entry.cell.get() == cell) write(entry);
            }
        }
    }

    public static void clear() {
        ENTRIES.clear();
    }

    @Nullable
    public static NBTTagCompound section(ItemStack cell, IAEStackType<?> type) {
        if (cell == null || type == null) return null;
        final NBTTagCompound root = ItemStackNBT.of(cell)
            .getCompoundTag(NBT_DATA);
        if (root == null || !root.hasKey(type.getId(), Constants.NBT.TAG_COMPOUND)) return null;
        return root.getCompoundTag(type.getId());
    }

    @NotNull
    public static NBTTagCompound emptyPage() {
        final NBTTagCompound out = new NBTTagCompound();
        out.setInteger(NBT_PAGE_TOTAL, 0);
        out.setTag(NBT_PAGE_ENTRIES, new NBTTagList());
        return out;
    }

    @NotNull
    public static NBTTagCompound page(DataStorage storage, int offset) {
        final ObjectList<IAEStack<?>> sorted = storage.getSortedContents(MAX_OFFSET + PAGE_SIZE);
        final NBTTagCompound out = new NBTTagCompound();
        out.setInteger(NBT_PAGE_TOTAL, (int) Math.min(Integer.MAX_VALUE, storage.getStoredTypes()));
        final NBTTagList entries = new NBTTagList();
        final int end = Math.min(offset + PAGE_SIZE, sorted.size());
        for (int i = Math.max(0, offset); i < end; i++) {
            final NBTTagCompound tag = new NBTTagCompound();
            sorted.get(i)
                .writeToNBT(tag);
            entries.appendTag(tag);
        }
        out.setTag(NBT_PAGE_ENTRIES, entries);
        return out;
    }

    private static Entry entry(DataStorage storage, IAEStackType<?> type) {
        final UUID uuid = storage.getRawUUID();
        final String typeId = type.getId();
        Object2ObjectMap<String, Entry> byType = ENTRIES.get(uuid);
        if (byType == null) {
            byType = new Object2ObjectOpenHashMap<>();
            ENTRIES.put(uuid, byType);
        }
        Entry entry = byType.get(typeId);
        if (entry == null) {
            entry = new Entry(uuid, type);
            byType.put(typeId, entry);
        }
        return entry;
    }

    private static void write(Entry e) {
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        final ItemStack cell = e.cell.get();
        if (manager == null || cell == null) return;
        final DataStorage storage = manager.getStorage(e.uuid, e.type);
        final NBTTagCompound section = new NBTTagCompound();
        section.setInteger(NBT_SNAPSHOT_TYPES, (int) Math.min(Integer.MAX_VALUE, storage.getStoredTypes()));
        section.setLong(NBT_SNAPSHOT_COUNT, storage.getStoredCount());
        writeSection(cell, e.type, section);
        e.dirty = false;
        e.lastWrite = System.currentTimeMillis();
    }

    private static void writeSection(ItemStack cell, IAEStackType<?> type, NBTTagCompound section) {
        final NBTTagCompound root = ItemStackNBT.get(cell);
        final NBTTagCompound data = root.getCompoundTag(NBT_DATA);
        data.setTag(type.getId(), section);
        root.setTag(NBT_DATA, data);
    }

    private static final class Entry {

        private final UUID uuid;
        private final IAEStackType<?> type;

        private WeakReference<ItemStack> cell = new WeakReference<>(null);
        private boolean dirty;
        private long lastWrite;

        private Entry(UUID uuid, IAEStackType<?> type) {
            this.uuid = uuid;
            this.type = type;
        }
    }
}
