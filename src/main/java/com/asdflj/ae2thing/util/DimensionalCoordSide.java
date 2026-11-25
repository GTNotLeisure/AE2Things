package com.asdflj.ae2thing.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.Constants;

import appeng.api.util.DimensionalCoord;

public final class DimensionalCoordSide extends DimensionalCoord {

    private final ForgeDirection side;
    private final String name;

    public DimensionalCoordSide(final int _x, final int _y, final int _z, final int _dim, ForgeDirection side,
        String name) {
        super(_x, _y, _z, _dim);
        this.side = side;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ForgeDirection getSide() {
        return this.side;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger(Constants.SIDE, this.side.ordinal());
        data.setString(Constants.NAME, this.name);
        super.writeToNBT(data);
    }

    public static DimensionalCoordSide readFromNBT(final NBTTagCompound data) {
        return new DimensionalCoordSide(
            data.getInteger("x"),
            data.getInteger("y"),
            data.getInteger("z"),
            data.getInteger("dim"),
            ForgeDirection.getOrientation(data.getInteger(Constants.SIDE)),
            data.getString(Constants.NAME));
    }
}
