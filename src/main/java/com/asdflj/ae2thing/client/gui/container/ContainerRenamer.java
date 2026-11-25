package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.util.DimensionalCoordSide;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;

public class ContainerRenamer extends AEBaseContainer {

    private DimensionalCoordSide renamingTarget;

    public ContainerRenamer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.bindPlayerInventory(ip, -1000, -1000);
    }

    public DimensionalCoordSide getRenamingTarget() {
        return this.renamingTarget;
    }

    public void setRenamingTarget(DimensionalCoordSide renamingTarget) {
        this.renamingTarget = renamingTarget;
    }
}
