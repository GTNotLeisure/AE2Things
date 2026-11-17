package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.common.storage.UniversalCellHandler;
import com.asdflj.ae2thing.common.storage.UniversalCellInventory;
import com.asdflj.ae2thing.common.storage.UniversalCellInventoryHandler;
import com.asdflj.ae2thing.coremod.hooker.CoreModHooks;
import com.asdflj.ae2thing.util.ItemStackNBT;
import com.asdflj.ae2thing.util.Util;

import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.storage.TileIOPort;
import appeng.util.ConfigManager;
import appeng.util.IterationCounter;
import appeng.util.Platform;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(TileIOPort.class)
public abstract class MixinTileIOPort extends AENetworkInvTile {

    @Shadow(remap = false)
    private ItemStack currentCell;

    @Shadow(remap = false)
    private IMEInventory<?> cachedInventory;

    @Shadow(remap = false)
    @Final
    private AppEngInternalInventory cells;

    @Shadow(remap = false)
    @Final
    private ConfigManager manager;

    @Shadow(remap = false)
    @Final
    private BaseActionSource mySrc;

    @Shadow(remap = false)
    private int[] moveQueue;

    @SuppressWarnings("unchecked")
    @Unique
    private final Map<String, IMEInventory<?>>[] ae2t$typeInventories = new Map[6];

    @Unique
    private final ItemStack[] ae2t$cachedCells = new ItemStack[6];

    @Unique
    private final boolean[] ae2t$didWorkBySlot = new boolean[6];

    @ModifyVariable(
        method = "transferContents",
        at = @At(value = "HEAD"),
        remap = false,
        argsOnly = true,
        name = "itemsToMove")
    private long transferContents(long itemsToMove) {
        return CoreModHooks.getItemsToMove((TileIOPort) (Object) this, itemsToMove);
    }

    @Inject(method = "getInv", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$universalCellInventory(ItemStack is, CallbackInfoReturnable<IMEInventory<?>> cir) {
        if (is == null || !(is.getItem() instanceof ItemInfinityStorageUniversalCell)) return;
        if (this.currentCell != is) {
            this.currentCell = is;
            this.cachedInventory = UniversalCellHandler.create(is, null, AEItemStackType.ITEM_STACK_TYPE);
        }
        cir.setReturnValue(this.cachedInventory);
    }

    @Inject(method = "shouldMove", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$shouldMove(IMEInventory<?> inventory, boolean sourceEmptyAfterTransfer, boolean destinationFull,
        boolean didWork, boolean moveOnEmptyWhileFilling, OperationMode om, FullnessMode fm,
        CallbackInfoReturnable<Boolean> cir) {
        if (!(inventory instanceof CellInventoryHandler<?>handler)) return;
        if (!(handler.getCellInv() instanceof UniversalCellInventory<?>cellInventory)) return;
        if (fm == FullnessMode.HALF) return;

        final ItemStack cellStack = cellInventory.getItemStack();
        if (om == OperationMode.FILL) {
            if (!moveOnEmptyWhileFilling) {
                cir.setReturnValue(false);
                return;
            }
            final IStorageGrid storage;
            try {
                storage = this.getProxy()
                    .getStorage();
            } catch (GridAccessException e) {
                cir.setReturnValue(false);
                return;
            }
            final int slot = this.ae2t$findSlot(cellStack);
            final boolean otherTypeWorked = slot >= 0 && this.ae2t$didWorkBySlot[slot];
            cir.setReturnValue(
                (didWork || otherTypeWorked) && sourceEmptyAfterTransfer && ae2t$otherTypesDrained(storage));
            return;
        }

        cir.setReturnValue(!ae2t$hasContent(cellStack));
    }

    @Inject(method = "tickingRequest", at = @At("RETURN"), remap = false)
    private void ae2t$transferOtherTypes(IGridNode node, int ticksSinceLastCall,
        CallbackInfoReturnable<TickRateModulation> cir) {
        if (!this.getProxy()
            .isActive()) return;
        if (!this.ae2t$hasUniversalCell()) return;

        for (int x = 0; x < 6; x++) {
            this.ae2t$didWorkBySlot[x] = false;
        }

        final OperationMode operationMode = (OperationMode) this.manager.getSetting(Settings.OPERATION_MODE);
        final FullnessMode fullnessMode = (FullnessMode) this.manager.getSetting(Settings.FULLNESS_MODE);
        final boolean empty = operationMode == OperationMode.EMPTY;
        final long amountToMove = CoreModHooks.getItemsToMove((TileIOPort) (Object) this, this.ae2t$amountToMove());

        final IEnergySource energy;
        final IStorageGrid storage;
        try {
            energy = this.getProxy()
                .getEnergy();
            storage = this.getProxy()
                .getStorage();
        } catch (GridAccessException e) {
            return;
        }

        for (int x = 0; x < 6; x++) {
            final ItemStack is = this.cells.getStackInSlot(x);
            if (is == null || !(is.getItem() instanceof ItemInfinityStorageUniversalCell)) continue;
            if (fullnessMode != FullnessMode.HALF && this.moveQueue[x] == 1) continue;

            for (final IAEStackType<?> type : Util.getTypes()) {
                if (type == AEItemStackType.ITEM_STACK_TYPE) continue;
                final IMEInventory<?> cellInventory = this.ae2t$cellInventory(is, x, type);
                if (cellInventory == null) continue;
                final IMEMonitor<?> monitor = storage.getMEMonitor(type);
                if (monitor == null) continue;

                final long budget = amountToMove * Math.max(1, type.getAmountPerUnit());
                final long moved = this
                    .ae2t$transfer(energy, empty ? cellInventory : monitor, empty ? monitor : cellInventory, budget);
                if (moved > 0) this.ae2t$didWorkBySlot[x] = true;
            }
        }
    }

    @Unique
    private int ae2t$findSlot(ItemStack cell) {
        for (int x = 0; x < 6; x++) {
            if (this.cells.getStackInSlot(x) == cell) return x;
        }
        return -1;
    }

    @Unique
    private static boolean ae2t$otherTypesDrained(IStorageGrid storage) {
        for (final IAEStackType<?> type : Util.getTypes()) {
            if (type == AEItemStackType.ITEM_STACK_TYPE) continue;
            final IMEMonitor<?> monitor = storage.getMEMonitor(type);
            if (monitor != null && !monitor.getStorageList()
                .isEmpty()) return false;
        }
        return true;
    }

    @Unique
    private boolean ae2t$hasUniversalCell() {
        for (int x = 0; x < 6; x++) {
            final ItemStack is = this.cells.getStackInSlot(x);
            if (is != null && is.getItem() instanceof ItemInfinityStorageUniversalCell) return true;
        }
        return false;
    }

    @Unique
    private static boolean ae2t$hasContent(ItemStack cell) {
        final StorageManager storageManager = AE2ThingAPI.instance()
            .getStorageManager();
        if (storageManager == null) return false;
        return storageManager.hasContent(ItemStackNBT.getString(cell, Constants.DISKUUID));
    }

    @Unique
    private long ae2t$amountToMove() {
        final TileIOPort self = (TileIOPort) (Object) this;
        long amount = 256;
        switch (self.getInstalledUpgrades(Upgrades.SPEED)) {
            case 1 -> amount *= 2;
            case 2 -> amount *= 4;
            case 3 -> amount *= 8;
        }
        switch (self.getInstalledUpgrades(Upgrades.SUPERSPEED)) {
            case 1 -> amount *= 16;
            case 2 -> amount *= 128;
            case 3 -> amount *= 1024;
        }
        switch (self.getInstalledUpgrades(Upgrades.SUPERLUMINALSPEED)) {
            case 1 -> amount *= 131_072;
            case 2 -> amount *= 8_388_608;
            case 3 -> amount *= 536_870_912;
        }
        return amount;
    }

    @Unique
    private IMEInventory<?> ae2t$cellInventory(ItemStack cell, int slot, IAEStackType<?> type) {
        if (this.ae2t$cachedCells[slot] != cell) {
            this.ae2t$cachedCells[slot] = cell;
            this.ae2t$typeInventories[slot] = new HashMap<>();
        }
        Map<String, IMEInventory<?>> byType = this.ae2t$typeInventories[slot];
        if (byType == null) {
            byType = new HashMap<>();
            this.ae2t$typeInventories[slot] = byType;
        }
        return byType.computeIfAbsent(type.getId(), _ -> UniversalCellHandler.create(cell, null, type));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Unique
    private static Iterable<? extends IAEStack> ae2t$stacksOf(IMEInventory<?> source) {
        if (source instanceof UniversalCellInventoryHandler<?>handler) {
            final IItemList<?> stored = handler.getStoredList();
            if (stored != null) return stored;
        }
        return ((IMEInventory) source).getAvailableItems(
            source.getStackType()
                .createList(),
            IterationCounter.fetchNewId());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Unique
    private long ae2t$transfer(IEnergySource energy, IMEInventory<?> source, IMEInventory<?> destination, long budget) {
        long moved = 0;
        final Iterator<? extends IAEStack> iterator = ae2t$stacksOf(source).iterator();

        while (iterator.hasNext() && budget > 0) {
            final IAEStack<?> stack = iterator.next();
            if (stack.getStackSize() <= 0) continue;

            final IAEStack request = stack.copy();
            request.setStackSize(Math.min(stack.getStackSize(), budget));

            final IAEStack simulated = Platform
                .poweredInsert(energy, (IMEInventory) destination, request, this.mySrc, Actionable.SIMULATE);
            final long possible = request.getStackSize() - (simulated == null ? 0 : simulated.getStackSize());
            if (possible <= 0) continue;
            request.setStackSize(possible);

            final IAEStack extracted = ((IMEInventory) source).extractItems(request, Actionable.MODULATE, this.mySrc);
            if (extracted == null) continue;

            final IAEStack failed = Platform
                .poweredInsert(energy, (IMEInventory) destination, extracted.setCraftable(false), this.mySrc);
            if (failed != null) ((IMEInventory) source).injectItems(failed, Actionable.MODULATE, this.mySrc);

            final long done = extracted.getStackSize() - (failed == null ? 0 : failed.getStackSize());
            budget -= done;
            moved += done;
        }

        return moved;
    }
}
