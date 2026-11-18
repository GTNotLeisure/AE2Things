package com.asdflj.ae2thing.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.common.ModernContainerPickup;

import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.helpers.MonitorableAction;

public final class ModernContainerInteraction {

    private ModernContainerInteraction() {}

    @Nullable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static MonitorableAction resolveAction(@Nullable IAEStack<?> hovered, @Nullable ItemStack hand,
        int mouseBtn) {
        if (!AE2ThingConfig.modernInteraction) return null;
        if (hand == null) return null;
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) return null;
        final boolean all = GuiScreen.isShiftKeyDown();

        switch (mouseBtn) {
            case 0 -> {
                if (hovered == null || hovered.getStackSize() <= 0) return null;
                final IAEStackType type = hovered.getStackType();
                if (type == null || !type.isContainerItemForType(hand)) return null;
                if (type.fillContainer(hand.copy(), hovered)
                    .rightLong() <= 0) return null;
                return all ? MonitorableAction.FILL_CONTAINERS : MonitorableAction.FILL_SINGLE_CONTAINER;
            }
            case 1 -> {
                for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
                    if (!type.isContainerItemForType(hand)) continue;
                    final IAEStack<?> contained = type.getStackFromContainerItem(hand);
                    if (contained == null || contained.getStackSize() <= 0) return null;
                    return all ? MonitorableAction.DRAIN_CONTAINERS : MonitorableAction.DRAIN_SINGLE_CONTAINER;
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    @Contract("null, _, _ -> false")
    public static boolean shouldFillFromNetwork(@Nullable IAEStack<?> hovered, @Nullable ItemStack hand, int mouseBtn) {
        if (!AE2ThingConfig.modernInteraction) return false;
        if (mouseBtn != 0 || hand != null) return false;
        if (hovered == null || hovered.getStackSize() <= 0) return false;
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) return false;
        return ModernContainerPickup.isSupported(hovered);
    }

    @Nullable
    public static String getContainedDisplayName(@Nullable ItemStack hand) {
        if (hand == null) return null;
        for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
            if (!type.isContainerItemForType(hand)) continue;
            final IAEStack<?> contained = type.getStackFromContainerItem(hand);
            return contained == null ? null : contained.getDisplayName();
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hidesEntryTooltip() {
        if (!AE2ThingConfig.modernInteraction) return false;
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return player != null && player.inventory.getItemStack() != null;
    }
}
