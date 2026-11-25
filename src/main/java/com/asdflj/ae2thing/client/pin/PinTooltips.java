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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean enabled() {
        return AE2ThingConfig.enhancedPins && Minecraft.getMinecraft().currentScreen instanceof GuiMEMonitorable gui
            && gui.hasPinHost;
    }

    private static VirtualMEPinSlot pinSlot(VirtualMEMonitorableSlot slot) {
        return slot instanceof VirtualMEPinSlot fixed ? fixed
            : slot instanceof ScrollingPlayerPinSlot scrolling ? scrolling.asPinSlot() : null;
    }

    public static boolean add(VirtualMEMonitorableSlot slot, List<String> lines) {
        if (!enabled()) return false;
        if (slot instanceof ScrollingPlayerPinSlot scrolling && scrolling.isPadding()) {
            lines.clear();
            return true;
        }
        VirtualMEPinSlot pin = pinSlot(slot);
        PinHand hand = PinHand.read();
        if (!AE2ThingConfig.modernInteraction) return false;
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
        return hand.isEmpty();
    }

    public static void addItem(VirtualMEMonitorableSlot slot, List<String> lines) {
        if (!enabled() || slot.getAEStack() == null
            || !PinHand.read()
                .isEmpty())
            return;
        addItemHints(lines, AE2ThingConfig.modernInteraction, pinSlot(slot) != null);
    }

    static void addItemHints(List<String> lines, boolean modern, boolean pin) {
        if (modern) lines.add(text("toggle"));
        if (pin) lines.add(text("remove"));
    }

    private static String text(String name) {
        return EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("ae2thing.gui.pin." + name);
    }
}
