package com.asdflj.ae2thing.coremod.mixin.nee;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.nei.terminal.TerminalCraftOverlay;
import com.github.vfyjxf.nee.nei.NEECraftingTerminalHandler;

import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;

@SuppressWarnings("UnusedMixin")
@Mixin(NEECraftingTerminalHandler.class)
public class MixinNeeCraftingTerminalHandler {

    @Inject(method = "isGuiCraftingTerm", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void ae2thing$recognizeWrappedHandler(GuiRecipe<?> gui, CallbackInfoReturnable<Boolean> cir) {
        if (gui == null || gui.firstGui == null) return;
        final IRecipeHandler handler = gui.getHandler();
        if (handler != null && TerminalCraftOverlay.isWrapped(handler.getOverlayHandler(gui.firstGui, 0))) {
            cir.setReturnValue(true);
        }
    }
}
