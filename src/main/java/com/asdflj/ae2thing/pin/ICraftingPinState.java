package com.asdflj.ae2thing.pin;

public interface ICraftingPinState {

    CraftingPinMode ae2t$craftingMode();

    boolean ae2t$isCraftingPinPending(int index);

    boolean ae2t$canCraftTarget();
}
