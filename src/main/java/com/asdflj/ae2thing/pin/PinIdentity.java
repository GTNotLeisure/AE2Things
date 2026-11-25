package com.asdflj.ae2thing.pin;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEStack;
import appeng.items.misc.ItemMEStackPacket;
import appeng.util.item.AEItemStack;

public final class PinIdentity {

    private PinIdentity() {}

    public static IAEStack<?> fromInventoryItem(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) return null;
        if (stack.getItem() instanceof ItemMEStackPacket) return ItemMEStackPacket.toAEStack(stack);
        return AEItemStack.create(stack);
    }

    public static boolean wasInserted(IAEStack<?> before, ItemStack remainder) {
        if (before == null || before.getStackSize() <= 0) return false;
        if (remainder == null) return true;
        IAEStack<?> after = fromInventoryItem(remainder);
        return after != null && wasInsertedAmount(before, after);
    }

    static boolean wasInsertedAmount(IAEStack<?> before, IAEStack<?> after) {
        if (before == null || before.getStackSize() <= 0) return false;
        if (after == null) return true;
        return before.isSameType(after) && after.getStackSize() >= 0 && after.getStackSize() < before.getStackSize();
    }
}
