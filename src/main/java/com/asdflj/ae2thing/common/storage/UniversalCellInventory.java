package com.asdflj.ae2thing.common.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;

public class UniversalCellInventory<T extends IAEStack<T>> implements ICellInventory<T> {

    private final ItemStack cellItem;
    private final ISaveProvider container;
    private final IAEStackType<T> type;
    private final IStorageCell cellType;
    private final IChestOrDrive drive;
    private final ItemStackNBT data;
    private final DataStorage storage;

    private IItemList<T> cellItems;

    public UniversalCellInventory(ItemStack cellItem, ISaveProvider container, IAEStackType<T> type)
        throws AppEngException {
        if (cellItem == null) throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        this.cellItem = cellItem;
        this.container = container;
        this.type = type;
        this.cellType = (IStorageCell) cellItem.getItem();
        this.drive = container instanceof IChestOrDrive chest ? chest : null;
        this.data = ItemStackNBT.of(cellItem);
        this.storage = this.resolveStorage();
    }

    private DataStorage resolveStorage() {
        if (!Platform.isServer()) return null;
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        if (manager == null) return null;
        final DataStorage storage = manager.getStorage(this.data.getString(Constants.DISKUUID), this.type);
        this.data.setString(Constants.DISKUUID, storage.getUUID());
        InfinityStorageCellSnapshot.markDirtyIfMissing(storage.getUUID(), this.type, this.cellItem);
        return storage;
    }

    @SuppressWarnings("unchecked")
    private IItemList<T> getCellItems() {
        if (this.cellItems == null) {
            final IItemList<?> list;
            if (this.storage == null) {
                list = this.type.createList();
            } else if (this.type == AEItemStackType.ITEM_STACK_TYPE) {
                list = this.storage.getItems();
            } else if (this.type == AEFluidStackType.FLUID_STACK_TYPE) {
                list = this.storage.getFluids();
            } else {
                list = this.storage.getOthers();
            }
            this.cellItems = (IItemList<T>) list;
        }
        return this.cellItems;
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain(this.cellItem);
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.cellItem);
    }

    @Override
    public IAEStackInventory getConfigAEInventory() {
        return this.cellType.getConfigAEInventory(this.cellItem);
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.cellItem);
    }

    @Override
    public boolean canHoldNewItem() {
        return true;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytesLong(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getUsedBytes() {
        final int weight = this.type.getAmountPerByte();
        final long countBytes = weight > 0 ? this.getStoredItemCount() / weight : 0;
        return Math.min(this.getTotalBytes(), countBytes + this.getStoredItemTypes() * (long) this.getBytesPerType());
    }

    @Override
    public long getTotalItemTypes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getStoredItemCount() {
        return this.storage == null ? 0 : this.storage.getStoredCount();
    }

    @Override
    public long getStoredItemTypes() {
        return this.storage == null ? 0 : this.storage.getStoredTypes();
    }

    @Override
    public long getRemainingItemTypes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getRemainingItemCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRemainingItemsCountDist(T l) {
        return 0;
    }

    @Override
    public int getUnusedItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        return 1;
    }

    @Override
    public String getOreFilter() {
        return this.cellType.getOreFilter(this.cellItem);
    }

    @Override
    public T injectItems(T input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() == 0) return null;
        if (input.getStackType() != this.type || this.cellType.isBlackListed(input)) return input;

        final long in = input.getStackSize();
        final IItemList<T> items = this.getCellItems();
        final T current = items.findPrecise(input);
        if (current != null) {
            if (mode == Actionable.MODULATE) {
                final boolean newType = current.getStackSize() <= 0;
                current.setStackSize(current.getStackSize() + in);
                this.addStored(in, newType ? 1 : 0);
                this.saveChanges(input.copy());
            }
            return null;
        }
        if (mode == Actionable.MODULATE) {
            items.add(input);
            this.addStored(in, 1);
            this.saveChanges(input.copy());
        }
        return null;
    }

    @Override
    public T extractItems(T request, Actionable mode, BaseActionSource src) {
        if (request == null || request.getStackSize() <= 0) return null;
        final T stored = this.getCellItems()
            .findPrecise(request);
        if (stored == null) return null;
        final long extracted = Math.min(stored.getStackSize(), request.getStackSize());
        if (extracted <= 0) return null;
        final T result = request.copy();
        result.setStackSize(extracted);
        if (mode == Actionable.MODULATE) {
            stored.setStackSize(stored.getStackSize() - extracted);
            this.addStored(-extracted, stored.getStackSize() <= 0 ? -1 : 0);
            final T change = request.copy();
            change.setStackSize(-extracted);
            this.saveChanges(change);
        }
        return result;
    }

    private void addStored(final long count, final long types) {
        if (this.storage != null) this.storage.addStored(count, types);
    }

    @Override
    public IItemList<T> getAvailableItems(IItemList<T> out, int iteration) {
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        if (this.storage != null && manager != null) manager.addGrid(this.storage.getUUID(), this.drive);
        for (final T stack : this.getCellItems()) {
            if (stack.getStackSize() > 0) out.add(stack);
        }
        return out;
    }

    @Override
    public @NotNull IAEStackType<?> getStackType() {
        return this.type;
    }

    private void saveChanges(T change) {
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        this.data.setBoolean(
            Constants.IS_EMPTY,
            this.storage == null || manager == null || !manager.hasContent(this.storage.getUUID()));
        if (this.storage != null) {
            InfinityStorageCellSnapshot.markDirty(this.storage.getUUID(), this.type, this.cellItem);
        }
        if (this.container != null) this.container.saveChanges(this);
        if (manager != null) {
            manager.postChange(this.storage, this.drive, change);
            manager.setDirty(true);
        }
    }
}
