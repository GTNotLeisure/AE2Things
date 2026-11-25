package com.asdflj.ae2thing.pin;

import java.util.List;

import appeng.api.storage.data.IAEStack;

public interface ICraftingPinActions {

    void ae2t$pruneCraftingPins(List<IAEStack<?>> outputs);

    long[] ae2t$pendingCraftingPins(List<IAEStack<?>> outputs);
}
