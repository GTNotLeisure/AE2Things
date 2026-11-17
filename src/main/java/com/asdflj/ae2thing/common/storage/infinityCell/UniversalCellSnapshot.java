package com.asdflj.ae2thing.common.storage.infinityCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;
import com.asdflj.ae2thing.common.storage.DataStorage;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class UniversalCellSnapshot {

    private static final long FLUSH_INTERVAL = 1000L;

    private static final Object2ObjectMap<String, Entry> ENTRIES = new Object2ObjectOpenHashMap<>();
    private static final WeakHashMap<ItemStack, Object2ObjectMap<String, CachedContents>> CACHE = new WeakHashMap<>();

    private UniversalCellSnapshot() {}

    public static void markDirty(String uuid, IAEStackType<?> type, ItemStack cell) {
        if (Platform.isClient() || uuid == null || uuid.isEmpty() || type == null || cell == null) return;
        final Entry entry = entry(uuid, type);
        entry.cell = cell;
        entry.dirty = true;
        entry.ver++;
    }

    public static void markDirtyIfMissing(String uuid, IAEStackType<?> type, ItemStack cell) {
        if (section(cell, type) != null) return;
        markDirty(uuid, type, cell);
    }

    public static void flushAll() {
        if (ENTRIES.isEmpty()) return;
        final long now = System.currentTimeMillis();
        for (final Entry entry : ENTRIES.values()) {
            if (!entry.dirty || now - entry.lastWrite < FLUSH_INTERVAL) continue;
            write(entry);
        }
    }

    public static void flushAllForce() {
        if (ENTRIES.isEmpty()) return;
        for (final Entry entry : ENTRIES.values()) {
            if (entry.dirty) write(entry);
        }
    }

    public static void flushNow(ItemStack cell) {
        if (ENTRIES.isEmpty() || cell == null) return;
        for (final Entry entry : ENTRIES.values()) {
            if (entry.dirty && entry.cell == cell) write(entry);
        }
    }

    public static void clear() {
        ENTRIES.clear();
        CACHE.clear();
    }

    private static Entry entry(String uuid, IAEStackType<?> type) {
        return ENTRIES.computeIfAbsent(uuid + '#' + type.getId(), _ -> new Entry(uuid, type));
    }

    private static void write(Entry e) {
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        if (manager == null || e.cell == null) return;

        final DataStorage storage = manager.getStorage(e.uuid, e.type);
        final NBTTagCompound section = new NBTTagCompound();
        section.setInteger(
            ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_TYPES,
            (int) Math.min(Integer.MAX_VALUE, storage.getStoredTypes()));
        section.setLong(ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_COUNT, storage.getStoredCount());
        section.setInteger(ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_VERSION, e.ver);

        final NBTTagList contents = new NBTTagList();
        for (final IAEStack<?> stack : storage.list()) {
            if (stack.getStackSize() <= 0) continue;
            contents.appendTag(stack.toNBTGeneric());
        }
        section.setTag(ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_CONTENTS, contents);
        writeSection(e.cell, e.type, section);

        e.dirty = false;
        e.lastWrite = System.currentTimeMillis();
    }

    private static void writeSection(ItemStack cell, IAEStackType<?> type, NBTTagCompound section) {
        final NBTTagCompound root = ItemStackNBT.get(cell);
        final NBTTagCompound data = root.getCompoundTag(ItemInfinityStorageUniversalCell.NBT_DATA);
        data.setTag(type.getId(), section);
        root.setTag(ItemInfinityStorageUniversalCell.NBT_DATA, data);
    }

    @Nullable
    public static NBTTagCompound section(ItemStack cell, IAEStackType<?> type) {
        if (cell == null || type == null) return null;
        final NBTTagCompound root = ItemStackNBT.of(cell)
            .getCompoundTag(ItemInfinityStorageUniversalCell.NBT_DATA);
        if (root == null || !root.hasKey(type.getId(), Constants.NBT.TAG_COMPOUND)) return null;
        return root.getCompoundTag(type.getId());
    }

    @NotNull
    public static List<IAEStack<?>> contents(ItemStack cell, IAEStackType<?> type) {
        final NBTTagCompound section = section(cell, type);
        if (section == null) return Collections.emptyList();

        final int ver = section.getInteger(ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_VERSION);
        final Object2ObjectMap<String, CachedContents> cache = CACHE
            .computeIfAbsent(cell, _ -> new Object2ObjectOpenHashMap<>());
        final CachedContents cached = cache.get(type.getId());
        if (cached != null && cached.ver == ver) return cached.stacks;

        final NBTTagList list = section
            .getTagList(ItemInfinityStorageUniversalCell.NBT_SNAPSHOT_CONTENTS, Constants.NBT.TAG_COMPOUND);
        final List<IAEStack<?>> stacks = new ArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            final IAEStack<?> stack = IAEStack.fromNBTGeneric(list.getCompoundTagAt(i));
            if (stack != null) stacks.add(stack);
        }
        final List<IAEStack<?>> result = Collections.unmodifiableList(stacks);
        cache.put(type.getId(), new CachedContents(ver, result));
        return result;
    }

    private static final class Entry {

        private final String uuid;
        private final IAEStackType<?> type;

        private ItemStack cell;
        private boolean dirty;
        private int ver;
        private long lastWrite;

        private Entry(String uuid, IAEStackType<?> type) {
            this.uuid = uuid;
            this.type = type;
        }
    }

    private record CachedContents(int ver, List<IAEStack<?>> stacks) {}
}
