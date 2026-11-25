package com.asdflj.ae2thing.coremod.mixin.ae;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.asdflj.ae2thing.pin.IPlayerPinActions;
import com.asdflj.ae2thing.pin.PlayerPinActions;

import appeng.api.config.PinsRows;
import appeng.api.storage.data.IAEStack;
import appeng.items.contents.PinList;
import appeng.items.contents.PinsHandler;
import appeng.items.contents.PinsHolder;

@SuppressWarnings("UnusedMixin")
@Mixin(PinsHandler.class)
public abstract class MixinPinsHandler implements IPlayerPinActions {

    @Shadow(remap = false)
    private PinList pinsInv;

    @Shadow(remap = false)
    @Final
    private PinsHolder holder;

    @Shadow(remap = false)
    private boolean needUpdate;

    @Shadow(remap = false)
    public abstract PinsRows getPlayerPinsRows();

    @Unique
    private PlayerPinActions ae2t$actions() {
        return new PlayerPinActions(pinsInv, getPlayerPinsRows().getSlotCount(), () -> {
            needUpdate = true;
            holder.markDirty();
        });
    }

    @Override
    public void ae2t$autoAdd(IAEStack<?> key) {
        ae2t$actions().autoAdd(key);
    }

    @Override
    public void ae2t$manualSetEmpty(int index, IAEStack<?> key) {
        ae2t$actions().manualSetEmpty(index, key);
    }

    @Override
    public void ae2t$toggle(IAEStack<?> key) {
        ae2t$actions().toggle(key);
    }

    @Override
    public void ae2t$removeActive(int index) {
        ae2t$actions().removeActive(index);
    }
}
