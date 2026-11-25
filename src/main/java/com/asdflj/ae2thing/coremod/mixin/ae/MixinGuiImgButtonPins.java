package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.client.pin.PinTooltips;
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
        boolean altHeld = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        String mode = PinTooltips.buttonMode(AE2ThingConfig.scrollPlayerPins);
        String details = PinTooltips.buttonDetails(altHeld, AE2ThingConfig.modernInteraction);
        cir.setReturnValue(
            mode + "\n"
                + String.join("\n", Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(details, 250)));
    }
}
