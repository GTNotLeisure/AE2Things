package com.asdflj.ae2thing.pin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public enum CraftingPinMode {

    AUTO,
    FIFO,
    LRU;

    private static final String KEY = "ae2thing.craftingPinMode";

    public static CraftingPinMode fromId(int id) {
        return id == 1 ? FIFO : id == 2 ? LRU : AUTO;
    }

    public static CraftingPinMode get(EntityPlayer player) {
        return fromId(
            player.getEntityData()
                .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG)
                .getInteger(KEY));
    }

    public static void cycle(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        data.setInteger(KEY, (get(player).ordinal() + 1) % values().length);
        player.getEntityData()
            .setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
    }
}
