package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.api.storage.data.IAEStack;

public interface IPlayerPinInventory {

    IAEStack<?> ae2t$inventoryPinKey(EntityPlayerMP player, int containerSlot);

    void ae2t$storeAndPin(EntityPlayerMP player, int containerSlot);
}
