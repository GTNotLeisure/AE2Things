package com.vcwdfca.appbot.client;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.implementations.IUpgradeableHost;
import appeng.container.implementations.ContainerUpgradeable;

public abstract class ContainerManaConfigurable extends ContainerUpgradeable {

    public ContainerManaConfigurable(InventoryPlayer ip, IUpgradeableHost te) {
        super(ip, te);
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }
}
