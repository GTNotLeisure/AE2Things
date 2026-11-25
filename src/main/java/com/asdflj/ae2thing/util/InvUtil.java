package com.asdflj.ae2thing.util;

import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.common.item.ItemPatternModifier;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.integration.snl.YogIntegration;
import com.asdflj.ae2thing.inventory.AEStackItemInventory;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;

import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public class InvUtil {

    // region Slot references

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

    // endregion
    // region Inventory lookup

    public static IntList getBackpackSlot(EntityPlayer player) {
        IntList result = new IntArrayList();
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            if (AE2ThingAPI.instance()
                .isBackpackItem(item)) {
                result.add(x);
            }
        }
        return result;
    }

    public static int findEquippableTerminal(EntityPlayer player, ItemStack is, Predicate<Item> condition) {
        IntObjectPair<ItemStack> found = scanItem(player, PlayerInvType.PLAYER_INV, is, condition);
        if (found != null) return found.leftInt();
        if (!AE2ThingConfig.terminalBaubles) return PlayerInvType.NO_SLOT;
        found = scanItem(player, PlayerInvType.PLAYER_BAUBLES, is, condition);
        return found != null ? found.leftInt() : PlayerInvType.NO_SLOT;
    }

    public static int findPatternModifier(EntityPlayer player) {
        IntObjectPair<ItemStack> found = scanItem(
            player,
            PlayerInvType.PLAYER_INV,
            null,
            item -> item instanceof ItemPatternModifier);
        return found != null ? found.leftInt() : PlayerInvType.NO_SLOT;
    }

    private static IntObjectPair<ItemStack> scanItem(EntityPlayer player, PlayerInvType type, ItemStack stack,
        Predicate<Item> condition) {
        if (type == PlayerInvType.PLAYER_BAUBLES && !Mods.BAUBLES.isModLoaded()) return null;
        int size = player.inventory.mainInventory.length;
        IntFunction<ItemStack> accessor = i -> player.inventory.getStackInSlot(i);
        if (type == PlayerInvType.PLAYER_BAUBLES) {
            final IInventory baubles = BaublesUtil.getBaublesInv(player);
            if (baubles == null) return null;
            size = baubles.getSizeInventory();
            accessor = baubles::getStackInSlot;
        }

        int fallbackSlot = PlayerInvType.NO_SLOT;
        ItemStack fallback = null;
        for (int i = 0; i < size; i++) {
            ItemStack item = accessor.apply(i);
            if (item == null || !(condition.test(item.getItem()))) continue;
            if (stack != null && Platform.isSameItemPrecise(item, stack)) {
                return new IntObjectImmutablePair<>(PlayerInvType.encode(i, type), item);
            }
            if (fallback == null) {
                fallbackSlot = i;
                fallback = item;
            }
        }
        return fallback == null ? null
            : new IntObjectImmutablePair<>(PlayerInvType.encode(fallbackSlot, type), fallback);
    }

    // endregion
    // region Inventory contents

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

    public static void clearAEInv(AEStackItemInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.putAEStackInSlot(i, null);
        }
    }

    public static void clearAEInv(AppEngInternalInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, null);
        }
    }

    public static void putCompact(AEStackItemInventory inventory, IAEStack<?>[] stacks) {
        int index = 0;
        for (IAEStack<?> stack : stacks) {
            if (stack != null) {
                inventory.putAEStackInSlot(index++, stack.copy());
            }
        }
    }

    // endregion
}
