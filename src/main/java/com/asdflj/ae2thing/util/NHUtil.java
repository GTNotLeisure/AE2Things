package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhlib.item.ItemStackNBT;

import appeng.util.Platform;

public final class NHUtil {

    public static NBTTagCompound getNBTData(@NotNull ItemStack stack) {
        return getNBTData(stack, false);
    }

    public static NBTTagCompound getNBTDataReadOnly(@NotNull ItemStack stack) {
        return getNBTData(stack, true);
    }

    private static NBTTagCompound getNBTData(@NotNull ItemStack stack, boolean readOnly) {
        NBTTagCompound data;
        try {
            data = ItemStackNBT.get(stack);
        } catch (Throwable e) {
            data = Platform.openNbtData(stack);
        }
        return readOnly ? (NBTTagCompound) data.copy() : data;
    }
}
