package com.asdflj.ae2thing.coremod.mixin.nei;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.nei.terminal.TerminalCraftOverlay;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.RecipeInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(RecipeInfo.class)
public class MixinRecipeInfo {

    @Inject(method = "getOverlayHandler", at = @At(value = "RETURN"), cancellable = true, remap = false)
    private static void ae2thing$wrapTerminalOverlay(GuiContainer gui, String ident,
        CallbackInfoReturnable<IOverlayHandler> cir) {
        final IOverlayHandler handler = cir.getReturnValue();
        if (handler == null || TerminalCraftOverlay.isWrapped(handler)) return;
        if (!TerminalCraftOverlay.isSupportedTerminal(gui)) return;
        cir.setReturnValue(TerminalCraftOverlay.wrap(handler));
    }
}
