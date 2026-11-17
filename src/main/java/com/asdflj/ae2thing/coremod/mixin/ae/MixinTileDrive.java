package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;
import com.asdflj.ae2thing.common.storage.UniversalCellHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;

import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackType;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.tile.storage.TileDrive;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(value = TileDrive.class)
public abstract class MixinTileDrive {

    @Shadow(remap = false)
    @Final
    private AppEngInternalInventory inv;

    @Shadow(remap = false)
    @Final
    private ICellHandler[] handlersBySlot;

    @Shadow(remap = false)
    @Final
    private Map<IAEStackType<?>, List<IMEInventoryHandler<?>>> cellsMap;

    @Shadow(remap = false)
    @Final
    private MEInventoryHandler<IAEItemStack>[] invBySlot;

    @Inject(method = "onChangeInventory", at = @At("HEAD"), remap = false)
    private void ae2t$flushSnapshot(final IInventory inv, final int slot, final InvOperation mc,
        final ItemStack removed, final ItemStack added, final CallbackInfo ci) {
        InfinityStorageCellSnapshot.flushNow(removed);
        InfinityStorageCellSnapshot.flushNow(added);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "updateState", at = @At("RETURN"), remap = false)
    private void ae2t$mountAllTypeInventories(CallbackInfo ci) {
        final TileDrive self = (TileDrive) (Object) this;
        for (int slot = 0; slot < this.inv.getSizeInventory(); slot++) {
            final ItemStack stack = this.inv.getStackInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemInfinityStorageUniversalCell)) continue;
            if (this.handlersBySlot[slot] == null) continue;
            if (this.invBySlot[slot] != null) continue;

            for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
                final IMEInventoryHandler<?> handler = UniversalCellHandler.create(stack, self, type, self);
                if (handler == null) continue;
                this.cellsMap.computeIfAbsent(type, _ -> new ArrayList<>())
                    .add(handler);
                if (this.invBySlot[slot] == null && type == AEItemStackType.ITEM_STACK_TYPE) {
                    this.invBySlot[slot] = (MEInventoryHandler<IAEItemStack>) handler;
                }
            }
        }
    }
}
