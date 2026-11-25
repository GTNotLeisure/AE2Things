package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.client.pin.PinTooltips;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiMEMonitorable.class)
public abstract class MixinGuiMEMonitorableEntryTooltip {

    @Inject(method = "handleItemTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$hideEntryTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip,
        CallbackInfoReturnable<List<String>> cir) {
        if (!ModernContainerInteraction.hidesEntryTooltip()) return;
        currentToolTip.clear();
        cir.setReturnValue(currentToolTip);
    }

    @Inject(method = "handleItemTooltip", at = @At("RETURN"), remap = false)
    private void ae2t$appendPinHints(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip,
        CallbackInfoReturnable<List<String>> cir) {
        if (((GuiMEMonitorable) (Object) this).getVirtualMESlotUnderMouse() instanceof VirtualMEMonitorableSlot slot)
            PinTooltips.addItem(slot, cir.getReturnValue());
    }
}
