package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Set;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEMonitor;
import appeng.me.cache.GridStorageCache;
import appeng.tile.storage.TileChest;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(GridStorageCache.class)
public abstract class MixinGridStorageCache {

    @Shadow(remap = false)
    public abstract Set<ICellProvider> getActiveCellProviders();

    @Shadow(remap = false)
    protected abstract void updateCellsStatusFromRegistry(ICellCacheRegistry iccr, ItemStack newCellStack);

    @Inject(method = "updateBytesInfo", at = @At("RETURN"), remap = false)
    private void ae2t$addUniversalCellBytes(final CallbackInfo ci) {
        for (final ICellProvider provider : this.getActiveCellProviders()) {
            if (!(provider instanceof final TileChest chest)) continue;
            final ItemStack cell = chest.getStackInSlot(1);
            if (cell == null || !(cell.getItem() instanceof ItemInfinityStorageUniversalCell)) continue;
            this.ae2t$addBytes(chest.getMEMonitor(AEItemStackType.ITEM_STACK_TYPE), cell);
            this.ae2t$addBytes(chest.getMEMonitor(AEFluidStackType.FLUID_STACK_TYPE), cell);
        }
    }

    @Unique
    private void ae2t$addBytes(final IMEMonitor<?> monitor, final ItemStack cell) {
        if (monitor != null && monitor.getInternal() instanceof ICellCacheRegistry registry) {
            this.updateCellsStatusFromRegistry(registry, cell);
        }
    }

}
