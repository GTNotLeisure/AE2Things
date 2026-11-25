package com.asdflj.ae2thing.client.pin;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.client.gui.slots.VirtualMEPinSlot;

public final class PinTooltips {

    private PinTooltips() {}

    public static boolean add(VirtualMEMonitorableSlot slot, List<String> lines) {
        if (!AE2ThingConfig.enhancedPins || !AE2ThingConfig.modernInteraction
            || !(Minecraft.getMinecraft().currentScreen instanceof GuiMEMonitorable gui)
            || !gui.hasPinHost) return false;
        if (slot instanceof ScrollingPlayerPinSlot scrolling && scrolling.isPadding()) {
            lines.clear();
            return true;
        }
        VirtualMEPinSlot pin = slot instanceof VirtualMEPinSlot fixed ? fixed
            : slot instanceof ScrollingPlayerPinSlot scrolling ? scrolling.asPinSlot() : null;
        PinHand hand = PinHand.read();
        if (pin != null && !pin.isCraftingSlot() && slot.getAEStack() == null && hand.effective() != null) {
            lines.clear();
            lines.add(text("set_body"));
            lines.add(text("set_content"));
            return true;
        }
        if (hand.ghost() != null) {
            lines.clear();
            return true;
        }
        if (hand.isEmpty()) {
            if (slot.getAEStack() != null) lines.add(text("toggle"));
            if (pin != null) lines.add(text("remove"));
            return true;
        }
        return false;
    }

    private static String text(String name) {
        return EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("ae2thing.gui.pin." + name);
    }
}
