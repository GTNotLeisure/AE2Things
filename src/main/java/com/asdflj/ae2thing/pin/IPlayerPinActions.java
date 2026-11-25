package com.asdflj.ae2thing.pin;

import appeng.api.storage.data.IAEStack;

public interface IPlayerPinActions {

    void ae2t$autoAdd(IAEStack<?> key);

    void ae2t$manualSetEmpty(int index, IAEStack<?> key);

    void ae2t$toggle(IAEStack<?> key);

    void ae2t$removeActive(int index);
}
