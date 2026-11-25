package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.pin.CraftingPinActions;
import com.asdflj.ae2thing.pin.ICraftingPinActions;
import com.asdflj.ae2thing.pin.IPlayerPinActions;
import com.asdflj.ae2thing.pin.PlayerPinActions;

import appeng.api.config.PinsRows;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.items.contents.PinList;
import appeng.items.contents.PinsHandler;
import appeng.items.contents.PinsHolder;

@SuppressWarnings("UnusedMixin")
@Mixin(PinsHandler.class)
public abstract class MixinPinsHandler implements IPlayerPinActions, ICraftingPinActions {

    @Shadow(remap = false)
    private PinList pinsInv;

    @Shadow(remap = false)
    @Final
    private PinsHolder holder;

    @Shadow(remap = false)
    private boolean needUpdate;

    @Shadow(remap = false)
    private PinsRows craftingPinsRows;

    @Shadow(remap = false)
    @Final
    private EntityPlayer player;

    @Unique
    private boolean ae2t$craftingRowsChanged;

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

    @Unique
    private CraftingPinActions ae2t$craftingActions() {
        return new CraftingPinActions(pinsInv, () -> {
            needUpdate = true;
            ae2t$syncCraftingRows();
            holder.markDirty();
        });
    }

    @Unique
    private void ae2t$syncCraftingRows() {
        if (!AE2ThingConfig.enhancedPins) return;
        PinsRows rows = PinsRows.fromOrdinal(ae2t$craftingActions().requiredRows());
        if (craftingPinsRows == rows) return;
        craftingPinsRows = rows;
        holder.setPinsRows(player, rows, getPlayerPinsRows());
        needUpdate = true;
        ae2t$craftingRowsChanged = true;
    }

    @Inject(method = "addItemsToPins", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$autoCraftingPins(Iterable<IAEStack<?>> pinsList, CallbackInfo ci) {
        if (!AE2ThingConfig.enhancedPins) return;
        ae2t$craftingActions().add(pinsList, Math.clamp(AEConfig.instance.maxCraftingPinRows, 1, 16) * 9);
        ae2t$syncCraftingRows();
        ci.cancel();
    }

    @Inject(method = "setPin", at = @At("TAIL"), remap = false)
    private void ae2t$compactCraftingPins(int idx, IAEStack<?> stack, CallbackInfo ci) {
        if (!AE2ThingConfig.enhancedPins || idx < 0 || idx >= PinList.CRAFTING_SLOTS) return;
        ae2t$craftingActions().compact();
        ae2t$syncCraftingRows();
    }

    @ModifyVariable(method = "setPinsRows", at = @At("HEAD"), argsOnly = true, remap = false, name = "craftingRows")
    private PinsRows ae2t$automaticCraftingRows(PinsRows craftingRows) {
        return AE2ThingConfig.enhancedPins ? PinsRows.fromOrdinal(ae2t$craftingActions().requiredRows()) : craftingRows;
    }

    @ModifyVariable(method = "update(Z)V", at = @At("HEAD"), argsOnly = true, remap = false, name = "forceSendPacket")
    private boolean ae2t$sendAutomaticRows(boolean forceSendPacket) {
        if (!AE2ThingConfig.enhancedPins) return forceSendPacket;
        ae2t$craftingActions().compact();
        ae2t$syncCraftingRows();
        return forceSendPacket || ae2t$craftingRowsChanged;
    }

    @Inject(method = "update(Z)V", at = @At("RETURN"), remap = false)
    private void ae2t$rowsSent(boolean forceSendPacket, CallbackInfo ci) {
        ae2t$craftingRowsChanged = false;
    }

    @Override
    public void ae2t$pruneCraftingPins(List<IAEStack<?>> outputs) {
        ae2t$craftingActions().pruneInactive(outputs);
        ae2t$syncCraftingRows();
    }

    @Override
    public long[] ae2t$pendingCraftingPins(List<IAEStack<?>> outputs) {
        return ae2t$craftingActions().pendingBits(outputs);
    }
}
