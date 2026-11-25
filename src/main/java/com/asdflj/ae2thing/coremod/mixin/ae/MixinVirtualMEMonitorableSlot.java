package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.client.localization.ButtonTooltips;
import com.asdflj.ae2thing.client.pin.PinTooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.client.gui.slots.VirtualMEMonitorableSlot;

@SuppressWarnings("UnusedMixin")
@Mixin(VirtualMEMonitorableSlot.class)
public abstract class MixinVirtualMEMonitorableSlot {

    @Inject(method = "addTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$modernInteractionTooltip(List<String> lines, CallbackInfo ci) {
        if (PinTooltips.add((VirtualMEMonitorableSlot) (Object) this, lines)) {
            ci.cancel();
            return;
        }
        if (!AE2ThingConfig.modernInteraction) return;

        lines.clear();
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            final ItemStack hand = player.inventory.getItemStack();
            final String contained = ModernContainerInteraction.getContainedDisplayName(hand);
            if (contained != null) {
                lines.add(storeAction(ButtonTooltips.left_click, hand.getDisplayName()));
                lines.add(storeAction(ButtonTooltips.right_click, contained));
            }
        }
        ci.cancel();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    private static String storeAction(ButtonTooltips click, String displayName) {
        return ButtonTooltips.store_action.getLocal(
            EnumChatFormatting.DARK_GRAY,
            click.getLocal(),
            EnumChatFormatting.GRAY + displayName + EnumChatFormatting.DARK_GRAY);
    }
}
