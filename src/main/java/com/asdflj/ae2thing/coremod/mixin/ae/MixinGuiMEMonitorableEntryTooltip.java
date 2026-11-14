package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.client.ModernContainerInteraction;

import appeng.client.gui.implementations.GuiMEMonitorable;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiMEMonitorable.class)
public abstract class MixinGuiMEMonitorableEntryTooltip {

    @Inject(method = "handleItemTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$hideEntryTooltip(ItemStack stack, int mouseX, int mouseY, List<String> tooltip,
        CallbackInfoReturnable<List<String>> cir) {
        if (!ModernContainerInteraction.hidesEntryTooltip()) return;
        tooltip.clear();
        cir.setReturnValue(tooltip);
    }
}
