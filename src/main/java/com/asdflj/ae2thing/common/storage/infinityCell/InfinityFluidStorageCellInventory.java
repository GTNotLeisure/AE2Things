package com.asdflj.ae2thing.common.storage.infinityCell;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.storage.DataStorage;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventory;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEFluidStackType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public class InfinityFluidStorageCellInventory implements ITFluidCellInventory {

    private final IChestOrDrive drive;
    protected IStorageCell cellType;
    protected final ItemStack cellItem;
    protected final ISaveProvider container;
    protected IItemList<IAEFluidStack> cellFluids = null;
    protected final ItemStackNBT data;
    protected final DataStorage storage;
    private boolean empty;

    public InfinityFluidStorageCellInventory(ItemStack o, ISaveProvider c) throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        this.drive = c instanceof IChestOrDrive ? (IChestOrDrive) c : null;
        this.cellItem = o;
        this.cellType = (IStorageCell) this.cellItem.getItem();
        this.container = c;
        this.data = ItemStackNBT.of(o);
        this.empty = this.data.getBoolean(Constants.IS_EMPTY);
        this.storage = this.getStorage();
        if (this.storage != null) {
            InfinityStorageCellSnapshot
                .markDirtyIfMissing(this.storage, AEFluidStackType.FLUID_STACK_TYPE, this.cellItem);
        }
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(input)) {
            return input;
        }
        final IAEFluidStack l = this.getCellFluids()
            .findPrecise(input);

        if (l != null) {
            if (mode == Actionable.MODULATE) {
                final long typeDelta = l.getStackSize() <= 0 ? 1 : 0;
                l.setStackSize(l.getStackSize() + input.getStackSize());
                this.saveChanges(input.copy(), typeDelta);
            }
            return null;
        }

        if (this.canHoldNewItem()) // room for new type, and for at least one item!
        {
            if (mode == Actionable.MODULATE) {
                this.cellFluids.add(input);
                this.saveChanges(input.copy(), 1);
            }
            return null;
        }

        return input;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }

        final long size = request.getStackSize();

        IAEFluidStack results = null;

        final IAEFluidStack l = this.getCellFluids()
            .findPrecise(request);

        if (l != null) {
            results = l.copy();

            if (l.getStackSize() <= size) {
                results.setStackSize(l.getStackSize());

                if (mode == Actionable.MODULATE) {
                    final long typeDelta = l.getStackSize() > 0 ? -1 : 0;
                    l.setStackSize(0);
                    IAEFluidStack change = results.copy();
                    change.setStackSize(-results.getStackSize());
                    this.saveChanges(change, typeDelta);
                }
            } else {
                results.setStackSize(size);

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    IAEFluidStack change = results.copy();
                    change.setStackSize(-results.getStackSize());
                    this.saveChanges(change, 0);
                }
            }
        }

        return results;
    }

    private void saveChanges(IAEFluidStack change, long typeDelta) {
        if (this.storage != null) this.storage.addStored(change.getStackSize(), typeDelta);
        final boolean empty = this.getCellFluids()
            .isEmpty();
        if (this.empty != empty) {
            this.empty = empty;
            this.data.setBoolean(Constants.IS_EMPTY, empty);
        }
        if (this.storage != null) {
            InfinityStorageCellSnapshot.markDirty(this.storage, AEFluidStackType.FLUID_STACK_TYPE, this.cellItem);
        }
        if (this.container != null) {
            this.container.saveChanges(this);
        }
        AE2ThingAPI.instance()
            .getStorageManager()
            .postFluidChange(this.storage, this.drive, change);
        AE2ThingAPI.instance()
            .getStorageManager()
            .setDirty(true);
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out, int iteration) {
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        if (this.storage != null && manager != null) manager.addGrid(this.storage.getRawUUID(), this.drive);
        for (final IAEFluidStack i : this.getCellFluids()) {
            out.add(i);
        }
        return out;
    }

    protected IItemList<IAEFluidStack> getCellFluids() {
        if (this.cellFluids == null) {
            this.loadCellFluids();
        }
        return this.cellFluids;
    }

    protected void loadCellFluids() {
        if (this.cellFluids == null) {
            if (this.storage == null) {
                this.cellFluids = AEApi.instance()
                    .storage()
                    .createFluidList();
            } else {
                this.cellFluids = this.storage.getFluids();
                this.empty = this.cellFluids.isEmpty();
            }
        }
        if (this.storage != null && !this.getUUID()
            .equals(this.storage.getUUID())) {
            data.setString(Constants.DISKUUID, this.storage.getUUID());
        }
    }

    @Override
    public String getUUID() {
        if (data.hasNoTags()) {
            return "";
        }
        return data.getString(Constants.DISKUUID);
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEFluidStackType.FLUID_STACK_TYPE;
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
    public double getIdleDrain(ItemStack is) {
        return this.cellType.getIdleDrain(is);
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
        return Integer.MAX_VALUE;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getStoredItemCount() {
        this.getCellFluids();
        return this.storage == null ? 0 : this.storage.getStoredCount();
    }

    @Override
    public long getRemainingItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getRemainingItemTypes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getUnusedItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        if (this.canHoldNewItem()) {
            return 1;
        }
        if (this.getRemainingItemCount() > 0) {
            return 2;
        }
        return 3;
    }

    @Override
    public long getStoredItemTypes() {
        this.getCellFluids();
        return this.storage == null ? 0 : this.storage.getStoredTypes();
    }

    @Override
    public long getTotalItemTypes() {
        return this.cellType.getTotalTypes(this.cellItem);
    }

    @Override
    public String getOreFilter() {
        return this.cellType.getOreFilter(this.cellItem);
    }

    @Override
    public List<IAEFluidStack> getContents() {
        if (Platform.isClient()) return ObjectLists.emptyList();
        List<IAEFluidStack> ret = new ObjectArrayList<>();
        for (IAEFluidStack fluid : this.getCellFluids()) {
            ret.add(fluid);
        }
        return ret;
    }

    @Override
    public IAEFluidStack getAvailableItem(@Nonnull IAEFluidStack request, int iteration) {
        IAEFluidStack available = this.getCellFluids()
            .findPrecise(request);
        return available == null ? null : available.copy();
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

}
