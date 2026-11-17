package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;
import com.asdflj.ae2thing.common.storage.UniversalCellHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStackType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.tile.storage.TileChest;

@SuppressWarnings("UnusedMixin")
@Mixin(value = TileChest.class)
public abstract class MixinTileChest extends AENetworkPowerTile {

    @Shadow(remap = false)
    @Final
    private AppEngInternalInventory inv;

    @SuppressWarnings("rawtypes")
    @Shadow(remap = false)
    @Final
    private Map<IAEStackType<?>, MEMonitorHandler> cellMap;

    @Shadow(remap = false)
    private boolean isCached;

    @Shadow(remap = false)
    private ICellHandler cellHandler;

    @SuppressWarnings("rawtypes")
    @Shadow(remap = false)
    private MEMonitorHandler<?> wrap(final IMEInventoryHandler h) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getHandler", at = @At("HEAD"), remap = false)
    private void ae2t$mountUniversalCell(final IAEStackType<?> type,
        final CallbackInfoReturnable<IMEInventoryHandler> cir) {
        if (this.isCached) return;
        final ItemStack cell = this.inv.getStackInSlot(1);
        if (cell == null || !(cell.getItem() instanceof ItemInfinityStorageUniversalCell)) return;

        this.cellMap.clear();
        this.cellHandler = AEApi.instance()
            .registries()
            .cell()
            .getHandler(cell);
        if (this.cellHandler == null) {
            this.isCached = true;
            return;
        }

        double power = 1.0;
        final ISaveProvider host = (ISaveProvider) this;
        for (IAEStackType<?> internalType : AEStackTypeRegistry.getAllTypes()) {
            final IMEInventoryHandler handler = UniversalCellHandler.create(cell, host, internalType);
            if (handler == null) continue;
            this.cellMap.put(internalType, this.wrap(handler));
            power += this.cellHandler.cellIdleDrain(cell, handler);
        }

        this.getProxy()
            .setIdlePowerUsage(power);
        this.isCached = true;
    }

    @Inject(method = "onChangeInventory", at = @At("HEAD"), remap = false)
    private void ae2t$flushSnapshot(final IInventory inv, final int slot, final InvOperation mc,
        final ItemStack removed, final ItemStack added, final CallbackInfo ci) {
        InfinityStorageCellSnapshot.flushNow(removed);
        InfinityStorageCellSnapshot.flushNow(added);
    }

    @Inject(method = "canInsertItem", at = @At("HEAD"), cancellable = true)
    private void ae2t$allowUniversalCell(final int slotIndex, final ItemStack insertingItem, final int side,
        final CallbackInfoReturnable<Boolean> cir) {
        if (slotIndex == 1 && insertingItem != null
            && insertingItem.getItem() instanceof ItemInfinityStorageUniversalCell) {
            cir.setReturnValue(true);
        }
    }
}
