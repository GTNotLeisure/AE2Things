package com.asdflj.ae2thing.common.storage;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEFluidStackType;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;

public class UniversalCellInventoryHandler<T extends IAEStack<T>> extends CellInventoryHandler<T> {

    public UniversalCellInventoryHandler(IMEInventory<T> c, IAEStackType<T> type) {
        super(c, type);
    }

    @Override
    public StorageChannel getStorageChannel() {
        return this.getCellInv() != null && this.getCellInv()
            .getStackType() == AEFluidStackType.FLUID_STACK_TYPE ? StorageChannel.FLUIDS : StorageChannel.ITEMS;
    }

    public IItemList<T> getStoredList() {
        final IMEInventory<T> inventory = this.getInternal();
        return inventory instanceof UniversalCellInventory<T>cell ? cell.getCellItems() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setPriorityList(boolean hasFuzzy, IAEStackInventory config, FuzzyMode fzMode) {
        final IAEStackType<T> type = (IAEStackType<T>) this.getCellInv()
            .getStackType();
        final IItemList<T> list = type.createList();
        if (config != null) {
            for (int i = 0; i < config.getSizeInventory(); i++) {
                final IAEStack<?> stack = config.getAEStackInSlot(i);
                if (stack == null || stack.getStackType() != type) continue;
                final T copy = (T) stack.copy();
                copy.setStackSize(1);
                list.add(copy);
            }
        }
        if (!list.isEmpty()) {
            this.setPartitionList(
                hasFuzzy && fzMode != null ? new FuzzyPriorityList<>(list, fzMode) : new PrecisePriorityList<>(list));
        }
    }
}
