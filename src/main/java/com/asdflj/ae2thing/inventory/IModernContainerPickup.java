package com.asdflj.ae2thing.inventory;

import appeng.api.storage.data.IAEStack;

/**
 * Implemented by containers that support taking a container from the network and filling it with the clicked entry when
 * the player's hand is empty.
 */
public interface IModernContainerPickup {

    /**
     * @param target       the entry that was clicked in the terminal
     * @param moveToPlayer whether the filled container is moved to the player inventory instead of the cursor
     */
    void fillFromNetwork(IAEStack<?> target, boolean moveToPlayer);
}
