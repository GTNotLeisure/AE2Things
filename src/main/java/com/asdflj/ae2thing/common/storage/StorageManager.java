package com.asdflj.ae2thing.common.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.BaseCellItem;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.core.AELog;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public class StorageManager extends WorldSavedData {

    private static final String SAVE_FORMAT = "format";
    private static final int SAVE_FORMAT_DISK_FILES = 2;

    private static final String DISK_SUFFIX = ".dat";
    private static final String DISK_TEMP_SUFFIX = ".tmp";
    private static final String DISK_BACKUP_SUFFIX = ".bak";

    private final Map<UUID, Map<IAEStackType<?>, DataStorage>> disks = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Set<IGrid>> grids = new Object2ObjectOpenHashMap<>();
    private final Set<UUID> dirtyDisks = new ObjectOpenHashSet<>();

    private File saveDirectory;
    private boolean legacyLoaded;
    private boolean legacyLoadFailed;
    private boolean legacyBackedUp;

    public StorageManager(String name) {
        super(name);
        this.setDirty(true);
        AE2ThingAPI.instance()
            .setStorageManager(this);
    }

    public DataStorage getStorage(String uuid, IAEStackType<?> stackType) {
        UUID uid;
        try {
            uid = UUID.fromString(uuid);
        } catch (Exception ignored) {
            do {
                uid = UUID.randomUUID();
            } while (this.disks.containsKey(uid));
        }
        final Map<IAEStackType<?>, DataStorage> byType = this.disks
            .computeIfAbsent(uid, _ -> new Object2ObjectOpenHashMap<>());
        DataStorage storage = byType.get(stackType);
        if (storage == null) {
            storage = new DataStorage(uid, stackType);
            byType.put(stackType, storage);
            this.dirtyDisks.add(uid);
        }
        return storage;
    }

    public boolean hasContent(String uuid) {
        if (uuid == null || uuid.isEmpty()) return false;
        try {
            return this.hasContent(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasContent(UUID uid) {
        final Map<IAEStackType<?>, DataStorage> byType = this.disks.get(uid);
        if (byType == null) return false;
        for (final DataStorage storage : byType.values()) {
            if (!storage.isEmpty()) return true;
        }
        return false;
    }

    private void addGrid(UUID uid, IGrid grid) {
        if (!Config.cellLink || grid == null || grid.isEmpty()) return;
        this.grids.putIfAbsent(uid, new ObjectOpenHashSet<>());
        this.grids.get(uid)
            .add(grid);
    }

    public void addGrid(String uuid, IGrid grid) {
        UUID uid = UUID.fromString(uuid);
        this.addGrid(uid, grid);
    }

    public void addGrid(String uuid, IChestOrDrive drive) {
        if (drive == null || uuid.isEmpty()) return;
        this.addGrid(
            uuid,
            drive.getActionableNode()
                .getGrid());
    }

    public DataStorage getStorage(ItemStack item) {
        if (item.getItem() instanceof BaseCellItem bci) {
            return this.getStorage(ItemStackNBT.getString(item, Constants.DISKUUID), bci.getStackType());
        }
        return null;
    }

    public DataStorage getStorage(ItemStack item, EntityPlayer player) {
        DataStorage storage = this.getStorage(item);
        if (storage == null) return null;
        ItemStackNBT data = ItemStackNBT.of(item);
        String uuid = data.getString(Constants.DISKUUID);
        if (uuid.isEmpty()) {
            data.setString(Constants.DISKUUID, storage.getUUID());
            player.inventory.setInventorySlotContents(player.inventory.currentItem, item.copy());
        }
        return storage;
    }

    public void setStorage(String uuid, ItemStack item) {
        ItemStackNBT data = ItemStackNBT.of(item);
        if (!data.getBoolean(Constants.LINKED) && !this.getStorage(item)
            .isEmpty()) return;
        String curUid = data.getString(Constants.DISKUUID);
        if (!curUid.isEmpty() && curUid.equals(uuid)) return;
        data.setString(Constants.DISKUUID, uuid);
        data.setBoolean(Constants.LINKED, true);
    }

    public void setSaveDirectory(File directory) {
        this.saveDirectory = directory;
    }

    public void loadFromDirectory() {
        if (this.saveDirectory == null) return;
        final File[] temps = this.saveDirectory.listFiles((_, name) -> name.endsWith(DISK_TEMP_SUFFIX));
        if (temps != null) {
            for (final File temp : temps) {
                try {
                    Files.deleteIfExists(temp.toPath());
                } catch (Exception ignored) {}
            }
        }
        final File[] files = this.saveDirectory.listFiles((_, name) -> name.endsWith(DISK_SUFFIX));
        if (files == null) return;
        for (final File file : files) {
            final String name = file.getName();
            final UUID uid = parseUuid(name.substring(0, name.length() - DISK_SUFFIX.length()));
            if (uid == null) continue;
            this.loadDisk(uid, file);
        }
    }

    public boolean flushDirtyDisks() {
        if (this.saveDirectory == null || this.dirtyDisks.isEmpty()) return true;
        final UUID[] dirty = this.dirtyDisks.toArray(new UUID[0]);
        this.dirtyDisks.clear();
        boolean ok = true;
        for (final UUID uid : dirty) {
            final Map<IAEStackType<?>, DataStorage> byType = this.disks.get(uid);
            if (byType == null || byType.isEmpty()) continue;
            if (!this.hasContent(uid)) {
                this.deleteDisk(uid);
                continue;
            }
            if (!this.writeDisk(uid, byType)) {
                this.dirtyDisks.add(uid);
                ok = false;
            }
        }
        return ok;
    }

    private void deleteDisk(UUID uid) {
        final File file = this.diskFile(uid);
        if (file == null) return;
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            AELog.error("[AE2Thing] Failed to delete disk %s: %s", file.getName(), e);
            return;
        }
        deleteQuietly(new File(file.getParentFile(), file.getName() + DISK_BACKUP_SUFFIX));
    }

    private void loadDisk(UUID uid, File file) {
        NBTTagCompound tag = readCompressed(file);
        if (tag == null) {
            final File backup = new File(file.getParentFile(), file.getName() + DISK_BACKUP_SUFFIX);
            tag = readCompressed(backup);
            if (tag == null) {
                AELog.warn("[AE2Thing] Failed to read disk %s, all of its contents are lost", file.getName());
                return;
            }
            AELog.warn("[AE2Thing] Recovered disk %s from its backup", file.getName());
        }

        for (final String key : tag.func_150296_c()) {
            final IAEStackType<?> type = AEStackTypeRegistry.getType(key);
            if (type == null) continue;
            final DataStorage storage = new DataStorage(uid, type);
            storage.readFromNBT(tag.getTagList(key, 10));
            put(this.disks, uid, storage);
        }
    }

    private boolean writeDisk(UUID uid, Map<IAEStackType<?>, DataStorage> byType) {
        final File file = this.diskFile(uid);
        if (file == null) return true;
        final NBTTagCompound tag = new NBTTagCompound();
        for (final DataStorage storage : byType.values()) {
            final IAEStackType<?> type = storage.getStackType();
            if (type == null) continue;
            tag.setTag(type.getId(), storage.writeToNBT());
        }
        return writeCompressed(file, tag);
    }

    private File diskFile(UUID uid) {
        if (this.saveDirectory == null) return null;
        return new File(this.saveDirectory, uid.toString() + DISK_SUFFIX);
    }

    private static boolean writeCompressed(File file, NBTTagCompound tag) {
        final File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            AELog.error("[AE2Thing] Failed to create disk directory %s", parent);
            return false;
        }

        final File temp = new File(parent, file.getName() + DISK_TEMP_SUFFIX);
        try (FileOutputStream out = new FileOutputStream(temp)) {
            CompressedStreamTools.writeCompressed(tag, out);
            out.flush();
        } catch (Exception e) {
            AELog.error("[AE2Thing] Failed to write disk %s: %s", file.getName(), e);
            deleteQuietly(temp);
            return false;
        }

        final File backup = new File(parent, file.getName() + DISK_BACKUP_SUFFIX);
        try {
            if (file.isFile()) {
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(
                    temp.toPath(),
                    file.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            AELog.error("[AE2Thing] Failed to replace disk %s: %s", file.getName(), e);
            deleteQuietly(temp);
            return false;
        }
        return true;
    }

    private static void deleteQuietly(File file) {
        if (file == null) return;
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception ignored) {}
    }

    private static NBTTagCompound readCompressed(File file) {
        if (file == null || !file.isFile()) return null;
        try (FileInputStream in = new FileInputStream(file)) {
            return CompressedStreamTools.readCompressed(in);
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void backupLegacyFile() {
        if (this.saveDirectory == null) return;
        final File parent = this.saveDirectory.getParentFile();
        if (parent == null) return;
        final File legacy = new File(parent, this.mapName + ".dat");
        if (!legacy.isFile()) return;
        try {
            Files.copy(
                legacy.toPath(),
                new File(parent, legacy.getName() + DISK_BACKUP_SUFFIX).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            AELog.error(e, "[AE2Thing] Failed to backup legacy storage file");
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        if (data.getInteger(SAVE_FORMAT) >= SAVE_FORMAT_DISK_FILES) {
            this.legacyLoaded = false;
            return;
        }

        final Map<UUID, Map<IAEStackType<?>, DataStorage>> d = new Object2ObjectOpenHashMap<>();
        try {
            final NBTTagList diskList = data.getTagList(Constants.DISKLIST, 10);
            for (int i = 0; i < diskList.tagCount(); i++) {
                final NBTTagCompound disk = diskList.getCompoundTagAt(i);
                final UUID uid = this.parseDiskUUID(disk);
                if (uid == null) continue;
                put(
                    d,
                    uid,
                    DataStorage
                        .readFromNBT(uid, disk.getTagList(Constants.DISKDATA, 10), AEItemStackType.ITEM_STACK_TYPE));
            }
            final NBTTagList fluidDiskList = data.getTagList(Constants.FLUID_DISKLIST, 10);
            for (int i = 0; i < fluidDiskList.tagCount(); i++) {
                final NBTTagCompound disk = fluidDiskList.getCompoundTagAt(i);
                final UUID uid = this.parseDiskUUID(disk);
                if (uid == null) continue;
                put(
                    d,
                    uid,
                    DataStorage.readFromNBT(
                        uid,
                        disk.getTagList(Constants.FLUID_DISKLIST, 10),
                        AEFluidStackType.FLUID_STACK_TYPE));
            }
            NBTTagList otherDiskList = data.getTagList(Constants.OTHER_DISKLIST, 10);
            for (int i = 0; i < otherDiskList.tagCount(); i++) {
                final NBTTagCompound disk = otherDiskList.getCompoundTagAt(i);
                final UUID uid = this.parseDiskUUID(disk);
                if (uid == null) continue;
                final IAEStackType<?> type = AEStackTypeRegistry.getType(disk.getString(Constants.DISKTYPE));
                if (type == null) continue;
                put(d, uid, DataStorage.readFromNBT(uid, disk.getTagList(Constants.OTHER_DISKLIST, 10), type));
            }
        } catch (Exception e) {
            AELog.error(e, "[AE2Thing] Failed to read legacy storage data, keeping previous state");
            this.legacyLoadFailed = true;
            return;
        }

        this.disks.clear();
        this.disks.putAll(d);
        this.dirtyDisks.addAll(d.keySet());
        this.legacyLoaded = !d.isEmpty();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        if (this.saveDirectory == null) {
            this.writeLegacy(data);
            return;
        }
        if (this.legacyLoadFailed && !this.legacyBackedUp) {
            this.legacyBackedUp = true;
            this.backupLegacyFile();
        }
        if (!this.flushDirtyDisks()) {
            this.writeLegacy(data);
            this.legacyLoaded = true;
            return;
        }
        if (this.legacyLoaded) {
            this.legacyLoaded = false;
            this.backupLegacyFile();
        }
        data.setInteger(SAVE_FORMAT, SAVE_FORMAT_DISK_FILES);
    }

    private void writeLegacy(NBTTagCompound data) {
        final NBTTagList diskList = new NBTTagList();
        final NBTTagList fluidDiskList = new NBTTagList();
        final NBTTagList otherDiskList = new NBTTagList();
        for (Map.Entry<UUID, Map<IAEStackType<?>, DataStorage>> entry : this.disks.entrySet()) {
            for (DataStorage storage : entry.getValue()
                .values()) {
                if (storage.isEmpty()) continue;
                final NBTTagCompound disk = new NBTTagCompound();
                disk.setString(
                    Constants.DISKUUID,
                    entry.getKey()
                        .toString());
                final IAEStackType<?> type = storage.getStackType();
                if (type != null) {
                    disk.setString(Constants.DISKTYPE, type.getId());
                }
                if (type == AEItemStackType.ITEM_STACK_TYPE) {
                    disk.setTag(Constants.DISKDATA, storage.writeToNBT());
                    diskList.appendTag(disk);
                } else if (type == AEFluidStackType.FLUID_STACK_TYPE) {
                    disk.setTag(Constants.FLUID_DISKLIST, storage.writeToNBT());
                    fluidDiskList.appendTag(disk);
                } else {
                    disk.setTag(Constants.OTHER_DISKLIST, storage.writeToNBT());
                    otherDiskList.appendTag(disk);
                }
            }
        }
        data.setTag(Constants.DISKLIST, diskList);
        data.setTag(Constants.FLUID_DISKLIST, fluidDiskList);
        data.setTag(Constants.OTHER_DISKLIST, otherDiskList);
    }

    private static void put(Map<UUID, Map<IAEStackType<?>, DataStorage>> map, UUID uid, DataStorage storage) {
        map.computeIfAbsent(uid, _ -> new Object2ObjectOpenHashMap<>())
            .put(storage.getStackType(), storage);
    }

    public void postItemChange(final DataStorage storage, IChestOrDrive drive, IAEItemStack change) {
        if (change == null || change.getStackSize() == 0) return;
        this.dirtyDisks.add(storage.getRawUUID());
        this.postChanges(
            storage,
            drive,
            storageGrid -> storageGrid
                .postAlterationOfStoredItems(AEItemStackType.ITEM_STACK_TYPE, ObjectLists.singleton(change), null));
    }

    public void postFluidChange(final DataStorage storage, IChestOrDrive drive, IAEFluidStack change) {
        if (change == null || change.getStackSize() == 0) return;
        this.dirtyDisks.add(storage.getRawUUID());
        this.postChanges(
            storage,
            drive,
            storageGrid -> storageGrid
                .postAlterationOfStoredItems(AEFluidStackType.FLUID_STACK_TYPE, ObjectLists.singleton(change), null));
    }

    public void postChange(final DataStorage storage, IChestOrDrive drive, IAEStack<?> change) {
        if (change == null || change.getStackSize() == 0) return;
        final IAEStackType<?> type = change.getStackType();
        if (type == null) return;
        this.dirtyDisks.add(storage.getRawUUID());
        this.postChanges(
            storage,
            drive,
            storageGrid -> storageGrid.postAlterationOfStoredItems(type, ObjectSets.singleton(change), null));
    }

    private void postChanges(final DataStorage storage, IChestOrDrive drive, Consumer<IStorageGrid> postChange) {
        if (!Config.cellLink || drive == null) return;
        if (drive instanceof AENetworkInvTile ait) {
            try {
                IGrid curGrid = ait.getProxy()
                    .getGrid();
                UUID uid = storage.getRawUUID();
                if (curGrid.isEmpty()) return;
                Set<IGrid> iGrids = this.grids.get(uid);
                if (iGrids == null) return;
                iGrids.removeIf(
                    g -> g.isEmpty() || !g.getPivot()
                        .isActive());
                if (iGrids.size() <= 1) return;
                Set<IGrid> cp = new ObjectOpenHashSet<>(iGrids);
                for (IGrid grid : cp) {
                    if (grid.isEmpty() || grid.equals(curGrid)) continue;
                    postChange.accept(grid.getCache(IStorageGrid.class));
                }
            } catch (Exception ignored) {}
        }
    }

    private UUID parseDiskUUID(NBTTagCompound tag) {
        final String raw = tag.getString(Constants.DISKUUID);
        if (raw.isEmpty()) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            AELog.warn(e, "[AE2Thing] Skipping corrupted disk entry, invalid uuid: " + raw);
            return null;
        }
    }
}
