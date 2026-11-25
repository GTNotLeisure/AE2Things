package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.widgets.GuiImgButton;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiImgButton.class)
public abstract class MixinGuiImgButtonPins {

    @Inject(method = "getMessage", at = @At("RETURN"), cancellable = true, remap = false)
    private void ae2t$pinModeHint(CallbackInfoReturnable<String> cir) {
        GuiImgButton button = (GuiImgButton) (Object) this;
        if (!AE2ThingConfig.enhancedPins || button.getSetting() != Settings.ACTIONS
            || button.getCurrentValue() != ActionItems.PINS
            || !(Minecraft.getMinecraft().currentScreen instanceof GuiMEMonitorable gui)
            || !gui.hasPinHost) return;
        String message = StatCollector.translateToLocal("ae2thing.gui.pin.rows") + "\n"
            + StatCollector.translateToLocal("ae2thing.gui.pin.crafting_auto");
        String mode = StatCollector.translateToLocal(
            AE2ThingConfig.scrollPlayerPins ? "ae2thing.gui.pin.scrolling" : "ae2thing.gui.pin.fixed");
        message = message + "\n" + mode + "\n" + StatCollector.translateToLocal("ae2thing.gui.pin.display");
        if (AE2ThingConfig.modernInteraction)
            message += "\n" + StatCollector.translateToLocal("ae2thing.gui.pin.inventory");
        cir.setReturnValue(message);
    }
}
