package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.common.item.BaseInfinityCellItem;

import appeng.integration.modules.NEIHelpers.NEICellViewHandler;
import codechicken.nei.recipe.IUsageHandler;

@SuppressWarnings("UnusedMixin")
@Mixin(NEICellViewHandler.class)
public abstract class MixinNEICellViewHandler {

    @Inject(method = "getUsageHandler", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$skipInfinityCells(String inputId, Object[] ingredients,
        CallbackInfoReturnable<IUsageHandler> cir) {
        if (ingredients == null || ingredients.length == 0 || !(ingredients[0] instanceof ItemStack stack)) return;
        if (stack.getItem() instanceof BaseInfinityCellItem<?>) {
            cir.setReturnValue(null);
        }
    }
}
