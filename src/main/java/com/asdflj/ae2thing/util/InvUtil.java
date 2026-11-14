package com.asdflj.ae2thing.util;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.integration.snl.YogIntegration;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;

import appeng.core.AELog;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public class InvUtil {

    public static IntList getSlotRefs(EntityPlayerMP player, Set<PlayerInvType> types) {
        IntList result = new IntArrayList();
        if (player == null || types == null || types.isEmpty()) {
            return result;
        }

        for (PlayerInvType type : PlayerInvType.values()) {
            if (!types.contains(type)) {
                continue;
            }

            int size = switch (type) {
                case PLAYER_INV -> player.inventory.mainInventory.length;

                case PLAYER_BAUBLES -> {
                    IInventory inventory = Mods.BAUBLES.isModLoaded() ? BaublesUtil.getBaublesInv(player) : null;

                    yield inventory == null ? 0 : inventory.getSizeInventory();
                }

                case YOG_STORAGE -> YogIntegration.isAvailable() ? YogIntegration.getStoredTerminalCount(player) : 0;
            };

            for (int slot = 0; slot < size; slot++) {
                result.add(PlayerInvType.encode(slot, type));
            }
        }

        return result;
    }

    public static ItemStack readSlotRef(EntityPlayer player, int x) {
        if (player == null) return null;
        if (x == PlayerInvType.NO_SLOT) return player.inventory.getCurrentItem();
        ObjectIntPair<PlayerInvType> decoded = PlayerInvType.decode(x);
        int slot = decoded.rightInt();
        if (slot < 0) return null;
        if (decoded.left() == PlayerInvType.PLAYER_BAUBLES) {
            if (!Mods.BAUBLES.isModLoaded()) return null;
            IInventory inv = BaublesUtil.getBaublesInv(player);
            return inv != null && slot < inv.getSizeInventory() ? inv.getStackInSlot(slot) : null;
        }
        if (decoded.left() == PlayerInvType.YOG_STORAGE) {
            return YogIntegration.isAvailable() ? YogIntegration.getStoredStack(player, slot) : null;
        }
        return slot < player.inventory.mainInventory.length ? player.inventory.getStackInSlot(slot) : null;
    }

    public static void writeSlotRef(EntityPlayer player, int x, ItemStack stack) {
        if (player == null) return;
        if (x == PlayerInvType.NO_SLOT) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
            return;
        }
        ObjectIntPair<PlayerInvType> decoded = PlayerInvType.decode(x);
        int slot = decoded.rightInt();
        if (slot < 0) return;
        if (decoded.left() == PlayerInvType.PLAYER_BAUBLES) {
            if (!Mods.BAUBLES.isModLoaded()) return;
            IInventory inv = BaublesUtil.getBaublesInv(player);
            if (inv == null || slot >= inv.getSizeInventory()) return;
            inv.setInventorySlotContents(slot, stack);
            inv.markDirty();
            return;
        }
        if (decoded.left() == PlayerInvType.YOG_STORAGE) {
            if (!YogIntegration.isAvailable()) return;
            if (!YogIntegration.setStoredStack(player, slot, stack)) {
                AELog.warn(
                    "Yog storage slot %s no longer holds the terminal that is being saved, skipping NBT write-back",
                    slot);
            }
            return;
        }
        if (slot < player.inventory.mainInventory.length) {
            player.inventory.setInventorySlotContents(slot, stack);
        }
    }

    public static int getMainSlotMissingCount(EntityPlayer player, ItemStack target, int slot) {
        if (player == null || target == null
            || target.getItem() == null
            || slot < 0
            || slot >= player.inventory.mainInventory.length) {
            return 0;
        }

        ItemStack destination = player.inventory.getStackInSlot(slot);

        if (destination != null && !Util.sameStackType(destination, target)) {
            return 0;
        }

        int maxSize = Math.min(target.getMaxStackSize(), player.inventory.getInventoryStackLimit());

        int currentSize = destination == null ? 0 : destination.stackSize;
        return Math.max(0, maxSize - currentSize);
    }

    public static int refillMainSlotFromInv(EntityPlayer player, ItemStack target, int slot) {
        int remaining = InvUtil.getMainSlotMissingCount(player, target, slot);
        if (remaining <= 0) {
            return 0;
        }

        int moved = 0;

        for (int sourceSlot = 0; sourceSlot < player.inventory.mainInventory.length && remaining > 0; sourceSlot++) {

            if (sourceSlot == slot) {
                continue;
            }

            ItemStack source = player.inventory.getStackInSlot(sourceSlot);

            if (source == null || source.stackSize <= 0 || !Util.sameStackType(source, target)) {
                continue;
            }

            int amount = Math.min(remaining, source.stackSize);
            ItemStack taken = player.inventory.decrStackSize(sourceSlot, amount);

            if (taken == null || taken.stackSize <= 0) {
                continue;
            }

            ItemStack destination = player.inventory.getStackInSlot(slot);

            if (destination == null) {
                player.inventory.setInventorySlotContents(slot, taken);
            } else {
                destination.stackSize += taken.stackSize;
            }

            moved += taken.stackSize;
            remaining -= taken.stackSize;
        }

        return moved;
    }
}
