package com.asdflj.ae2thing.pin;

import appeng.api.storage.data.IAEStack;
import appeng.items.contents.PinList;

public record PlayerPinActions(PinList pins, int capacity, Runnable dirty) {

    public PlayerPinActions(PinList pins, int capacity, Runnable dirty) {
        this.pins = pins;
        this.capacity = Math.clamp(capacity, 0, PinList.PLAYER_SLOTS);
        this.dirty = dirty;
    }

    public boolean isActive(int index) {
        return index >= PinList.PLAYER_OFFSET && index < PinList.PLAYER_OFFSET + capacity;
    }

    private boolean contains(IAEStack<?> key) {
        for (int i = PinList.PLAYER_OFFSET; i < PinList.TOTAL_SLOTS; i++) {
            IAEStack<?> current = pins.getPin(i);
            if (current != null && current.isSameType(key)) return true;
        }
        return false;
    }

    public boolean autoAdd(IAEStack<?> key) {
        if (key == null || contains(key)) return false;
        for (int i = PinList.PLAYER_OFFSET; isActive(i); i++) {
            if (pins.getPin(i) == null) return manualSetEmpty(i, key);
        }
        return false;
    }

    public boolean manualSetEmpty(int index, IAEStack<?> key) {
        if (key == null || !isActive(index) || pins.getPin(index) != null || contains(key)) return false;
        IAEStack<?> copy = key.copy();
        copy.setStackSize(0);
        pins.setPin(index, copy);
        dirty.run();
        return true;
    }

    public boolean toggle(IAEStack<?> key) {
        if (key == null) return false;
        boolean removed = false;
        for (int i = PinList.PLAYER_OFFSET; i < PinList.TOTAL_SLOTS; i++) {
            IAEStack<?> current = pins.getPin(i);
            if (current != null && current.isSameType(key)) {
                pins.setPin(i, null);
                removed = true;
            }
        }
        if (!removed) return autoAdd(key);
        dirty.run();
        return true;
    }

    public boolean removeActive(int index) {
        if (!isActive(index) || pins.getPin(index) == null) return false;
        pins.setPin(index, null);
        dirty.run();
        return true;
    }
}
