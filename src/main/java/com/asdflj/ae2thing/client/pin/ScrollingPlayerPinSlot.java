package com.asdflj.ae2thing.client.pin;

import appeng.api.storage.data.IDisplayRepo;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.client.gui.slots.VirtualMEPinSlot;

public final class ScrollingPlayerPinSlot extends VirtualMEMonitorableSlot {

    public ScrollingPlayerPinSlot(int x, int y, IDisplayRepo repo, int index, TypeFilterChecker checker) {
        super(x, y, repo, index, checker);
    }

    public int playerPinIndex() {
        return ((IPlayerPinRepo) repo).ae2t$playerPinIndex(slotIndex);
    }

    public boolean isPadding() {
        return ((IPlayerPinRepo) repo).ae2t$isPinPadding(slotIndex);
    }

    public VirtualMEPinSlot asPinSlot() {
        int index = playerPinIndex();
        return index < 0 ? null : new VirtualMEPinSlot(getX(), getY(), repo, index, typeFilterChecker, false);
    }
}
