package com.asdflj.ae2thing.coremod.mixin.nei;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.nei.terminal.TerminalCraftOverlay;
import com.asdflj.ae2thing.nei.terminal.TerminalStockCache;

import codechicken.nei.ItemStackAmount;
import codechicken.nei.recipe.AutoCraftingManager;

@SuppressWarnings("UnusedMixin")
@Mixin(AutoCraftingManager.class)
public class MixinAutoCraftingManager {

    @Inject(method = "getInventoryItems", at = @At(value = "RETURN"), remap = false)
    private static void ae2thing$addTerminalStock(GuiContainer guiContainer,
        CallbackInfoReturnable<ItemStackAmount> cir) {
        if (!TerminalCraftOverlay.isSupportedTerminal(guiContainer)) return;

        final ItemStackAmount stock = TerminalStockCache.get(guiContainer);
        if (stock == null) return;

        final ItemStackAmount inventory = cir.getReturnValue();
        if (inventory == null) return;
        inventory.putAll(stock);
    }
}
