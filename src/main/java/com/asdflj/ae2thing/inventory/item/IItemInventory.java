package com.asdflj.ae2thing.inventory.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.asdflj.ae2thing.inventory.gui.PlayerInvType;

public interface IItemInventory {

    Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player);

    default Object getInventoryAt(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return getInventory(stack, world, PlayerInvType.slotOf(x), y, z, player);
    }
}
