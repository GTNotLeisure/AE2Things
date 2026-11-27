package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.client.localization.ButtonTooltips;
import com.asdflj.ae2thing.client.pin.PinTooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.pin.CraftingPinMode;
import com.asdflj.ae2thing.pin.ICraftingPinState;

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
        String original = cir.getReturnValue();
        if (original == null) return;
        int lineBreak = original.indexOf('\n');
        String title = lineBreak < 0 ? original : original.substring(0, lineBreak);
        String mode = title + "\n" + PinTooltips.buttonMode(AE2ThingConfig.scrollPlayerPins);
        CraftingPinMode crafting = ((ICraftingPinState) gui.inventorySlots).ae2t$craftingMode();
        ButtonTooltips craftingLabel = ButtonTooltips.mode_auto;
        if (crafting == CraftingPinMode.FIFO) craftingLabel = ButtonTooltips.mode_fifo;
        if (crafting == CraftingPinMode.LRU) craftingLabel = ButtonTooltips.mode_lru;
        mode += "\n" + craftingLabel.getLocal();
        if (!Keyboard.isKeyDown(Keyboard.KEY_LMENU) && !Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            cir.setReturnValue(mode + "\n" + ButtonTooltips.hold_alt.getLocal());
            return;
        }
        String details = PinTooltips.buttonDetails(AE2ThingConfig.modernInteraction);
        if (crafting != CraftingPinMode.AUTO) details += "\n" + ButtonTooltips.crafting_rows.getLocal();
        cir.setReturnValue(
            mode + "\n"
                + String.join("\n", Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(details, 250)));
    }
}
