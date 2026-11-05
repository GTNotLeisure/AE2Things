package com.asdflj.ae2thing.inventory.gui;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public enum PlayerInvType {

    PLAYER_INV,
    PLAYER_BAUBLES,
    YOG_STORAGE;

    private static final int FLAG = 1 << 30;
    private static final int TYPE_SHIFT = 28;
    private static final int TYPE_MAX = (1 << 2) - 1;
    private static final int SLOT_MASK = (1 << TYPE_SHIFT) - 1;

    public static final int NO_SLOT = -1;

    public static int encode(int slot, PlayerInvType type) {
        if (slot == NO_SLOT) return NO_SLOT;
        if (slot < 0 || slot > SLOT_MASK) {
            throw new IllegalArgumentException("slot out of range" + slot);
        }
        return FLAG | (type.ordinal() << TYPE_SHIFT) | slot;
    }

    public static ObjectIntPair<PlayerInvType> decode(int value) {
        if (value != NO_SLOT && (value & FLAG) != 0) {
            return new ObjectIntImmutablePair<>(values()[(value >> TYPE_SHIFT) & TYPE_MAX], value & SLOT_MASK);
        }
        return new ObjectIntImmutablePair<>(PLAYER_INV, value);
    }

    public static PlayerInvType typeOf(int value) {
        return decode(value).left();
    }

    public static int slotOf(int value) {
        return decode(value).rightInt();
    }
}
