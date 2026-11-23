package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.integration.Mods;
import com.science.gtnl.utils.enums.GTNLItemList;

import appeng.util.Platform;

public final class GTNLUtil {

    private GTNLUtil() {}

    public static boolean isAssemblerMatrix(ItemStack stack) {
        if (stack == null || stack.getItem() == null || !Mods.SNL.isModLoaded()) return false;
        try {
            final ItemStack matrix = GTNLItemList.AssemblerMatrix.get(1);
            return matrix != null && Platform.isSameItemPrecise(matrix, stack);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
