package com.asdflj.ae2thing.pin;

import java.util.List;

import appeng.api.storage.data.IAEStack;
import appeng.items.contents.PinList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public record CraftingPinActions(PinList pins, Runnable dirty) {

    public boolean add(Iterable<? extends IAEStack<?>> outputs, int capacity, CraftingPinMode mode) {
        capacity = Math.clamp(capacity, 0, PinList.CRAFTING_SLOTS);
        if (capacity == 0) return false;
        List<IAEStack<?>> retained = entries();
        boolean added = false;
        for (IAEStack<?> output : outputs) {
            if (output == null) continue;
            if (mode == CraftingPinMode.FIFO && CraftingPinState.isPending(output, retained)) continue;
            IAEStack<?> key = output.copy();
            key.setStackSize(0);
            retained.removeIf(existing -> existing.isSameType(key));
            retained.addFirst(key);
            if (retained.size() > capacity) retained.subList(capacity, retained.size())
                .clear();
            added = true;
        }
        return added && replace(retained);
    }

    public int requiredRows() {
        for (int i = PinList.CRAFTING_SLOTS - 1; i >= 0; i--) {
            if (pins.getPin(i) != null) return i / 9 + 1;
        }
        return 0;
    }

    public boolean touch(IAEStack<?> key) {
        if (key == null) return false;
        for (int i = 0; i < PinList.CRAFTING_SLOTS; i++) {
            IAEStack<?> existing = pins.getPin(i);
            if (existing == null) break; // Crafting entries are compacted in recency order.
            if (!existing.isSameType(key)) continue;
            if (i == 0) return false;
            for (int j = i; j > 0; j--) pins.setPin(j, pins.getPin(j - 1));
            pins.setPin(0, existing);
            dirty.run();
            return true;
        }
        return false;
    }

    public boolean compact() {
        return replace(entries());
    }

    private List<IAEStack<?>> entries() {
        List<IAEStack<?>> retained = new ObjectArrayList<>();
        for (int i = 0; i < PinList.CRAFTING_SLOTS; i++) {
            IAEStack<?> key = pins.getPin(i);
            if (key != null) retained.add(key);
        }
        return retained;
    }

    public boolean pruneInactive(List<IAEStack<?>> outputs) {
        List<IAEStack<?>> retained = new ObjectArrayList<>();
        for (int i = 0; i < PinList.CRAFTING_SLOTS; i++) {
            IAEStack<?> key = pins.getPin(i);
            if (CraftingPinState.isPending(key, outputs)) retained.add(key);
        }
        return replace(retained);
    }

    private boolean replace(List<IAEStack<?>> retained) {
        boolean changed = false;
        for (int i = 0; i < PinList.CRAFTING_SLOTS; i++) {
            IAEStack<?> key = i < retained.size() ? retained.get(i) : null;
            if (pins.getPin(i) != key) {
                pins.setPin(i, key);
                changed = true;
            }
        }
        if (changed) dirty.run();
        return changed;
    }

    public long[] pendingBits(List<IAEStack<?>> outputs) {
        long[] bits = new long[CraftingPinState.WORDS];
        for (int i = 0; i < PinList.CRAFTING_SLOTS; i++) {
            if (CraftingPinState.isPending(pins.getPin(i), outputs)) bits[i / Long.SIZE] |= 1L << (i % Long.SIZE);
        }
        return bits;
    }
}
