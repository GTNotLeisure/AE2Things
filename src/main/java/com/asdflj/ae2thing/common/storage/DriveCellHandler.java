package com.asdflj.ae2thing.common.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.storage.TileDrive;

public class DriveCellHandler<T extends IAEStack<T>> extends MEInventoryHandler<T> {

    private final TileDrive drive;

    public DriveCellHandler(IMEInventoryHandler<T> inner, IAEStackType<T> type, TileDrive drive) {
        super(inner, type);
        this.drive = drive;
    }

    @Override
    public AccessRestriction getReshuffleAccess() {
        return this.drive.getReshuffleAccess();
    }

    @Override
    public int getPriority() {
        return this.drive.getPriority();
    }
}
