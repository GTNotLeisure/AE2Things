package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public final class ItemGuiHintCache {

    private static final int MAX_ENTRIES = 16;

    private static final Int2ObjectMap<ItemStack> HINTS = new Int2ObjectLinkedOpenHashMap<>();

    private ItemGuiHintCache() {}

    public static void put(int x, ItemStack stack) {
        if (stack == null) return;
        HINTS.put(x, stack.copy());
        if (HINTS.size() > MAX_ENTRIES) {
            final IntIterator it = HINTS.keySet()
                .iterator();
            if (it.hasNext()) {
                it.nextInt();
                it.remove();
            }
        }
    }

    public static ItemStack get(int x) {
        final ItemStack stack = HINTS.get(x);
        return stack == null ? null : stack.copy();
    }

    public static void clear() {
        HINTS.clear();
    }
}
