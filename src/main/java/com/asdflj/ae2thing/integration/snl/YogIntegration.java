package com.asdflj.ae2thing.integration.snl;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.integration.Mods;
import com.science.gtnl.api.YogSothothApi;

import appeng.util.Platform;
import cpw.mods.fml.common.Optional;

public final class YogIntegration {

    private static final String MODID = "sciencenotleisure";

    private YogIntegration() {}

    public static boolean isAvailable() {
        return Mods.SNL.isModLoaded();
    }

    @Optional.Method(modid = MODID)
    public static ItemStack getStoredStack(EntityPlayer player, int slot) {
        final List<ItemStack> stored = storedTerminals(player);
        if (stored == null || slot < 0 || slot >= stored.size()) return null;
        return stored.get(slot);
    }

    @Optional.Method(modid = MODID)
    public static int indexOf(EntityPlayer player, ItemStack stack) {
        if (stack == null) return -1;
        final List<ItemStack> stored = storedTerminals(player);
        if (stored == null || stored.isEmpty()) return -1;
        for (int i = 0; i < stored.size(); i++) {
            if (stored.get(i) == stack) return i;
        }
        for (int i = 0; i < stored.size(); i++) {
            if (Platform.isSameItemPrecise(stored.get(i), stack)) return i;
        }
        return -1;
    }

    @Optional.Method(modid = MODID)
    private static List<ItemStack> storedTerminals(EntityPlayer player) {
        return player == null ? null : YogSothothApi.storedTerminals(player);
    }

    @Optional.Method(modid = MODID)
    public static boolean setStoredStack(EntityPlayer player, int slot, ItemStack stack) {
        return getStoredStack(player, slot) == stack;
    }
}
