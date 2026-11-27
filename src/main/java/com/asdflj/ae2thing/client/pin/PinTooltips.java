package com.asdflj.ae2thing.client.pin;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import com.asdflj.ae2thing.client.localization.ButtonTooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.pin.CraftingPinMode;
import com.asdflj.ae2thing.pin.ICraftingPinState;

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
            lines.add(ButtonTooltips.set_body.getLocal(EnumChatFormatting.DARK_GRAY));
            lines.add(ButtonTooltips.set_content.getLocal(EnumChatFormatting.DARK_GRAY));
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
        VirtualMEPinSlot pin = pinSlot(slot);
        boolean removable = pin != null && (!pin.isCraftingSlot()
            || ((ICraftingPinState) Minecraft.getMinecraft().thePlayer.openContainer).ae2t$craftingMode()
                != CraftingPinMode.AUTO);
        addItemHints(lines, AE2ThingConfig.modernInteraction, removable);
    }

    static void addItemHints(List<String> lines, boolean modern, boolean pin) {
        if (modern) lines.add(ButtonTooltips.toggle.getLocal(EnumChatFormatting.DARK_GRAY));
        if (pin) lines.add(ButtonTooltips.remove.getLocal(EnumChatFormatting.DARK_GRAY));
    }

    public static String buttonMode(boolean scrolling) {
        final ButtonTooltips mode = scrolling ? ButtonTooltips.scrolling : ButtonTooltips.fixed;
        return mode.getLocal();
    }

    public static String buttonDetails(boolean modern) {
        String details = ButtonTooltips.rows.getLocal() + "\n"
            + ButtonTooltips.display.getLocal()
            + "\n"
            + ButtonTooltips.remove.getLocal();
        if (modern) details += "\n" + ButtonTooltips.inventory.getLocal();
        return details.replace("\\n", "\n");
    }
}
