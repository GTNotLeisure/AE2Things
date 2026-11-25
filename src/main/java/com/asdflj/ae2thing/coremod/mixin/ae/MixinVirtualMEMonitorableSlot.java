package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.client.pin.PinTooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.client.gui.slots.VirtualMEMonitorableSlot;

@SuppressWarnings("UnusedMixin")
@Mixin(VirtualMEMonitorableSlot.class)
public abstract class MixinVirtualMEMonitorableSlot {

    @Unique
    private static final String KEY_STORE_ACTION = "ae2thing.gui.terminal.store_action";
    @Unique
    private static final String KEY_LEFT_CLICK = "ae2thing.gui.terminal.left_click";
    @Unique
    private static final String KEY_RIGHT_CLICK = "ae2thing.gui.terminal.right_click";

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
                lines.add(storeAction(KEY_LEFT_CLICK, hand.getDisplayName()));
                lines.add(storeAction(KEY_RIGHT_CLICK, contained));
            }
        }
        ci.cancel();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    private static String storeAction(String clickKey, String displayName) {
        return EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocalFormatted(
            KEY_STORE_ACTION,
            StatCollector.translateToLocal(clickKey),
            EnumChatFormatting.GRAY + displayName + EnumChatFormatting.DARK_GRAY);
    }
}
