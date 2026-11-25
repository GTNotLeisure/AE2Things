package com.asdflj.ae2thing.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.DimensionalCoord;

public record BlockPos(int x, int y, int z, World w) {

    public BlockPos(int x, int y, int z) {
        this(x, y, z, null);
    }

    public BlockPos(@Nonnull TileEntity te) {
        this(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj());
    }

    public BlockPos(@Nonnull MovingObjectPosition te, World w) {
        this(te.blockX, te.blockY, te.blockZ, w);
    }

    public BlockPos(DimensionalCoord dimensionalCoord) {
        this(dimensionalCoord.x, dimensionalCoord.y, dimensionalCoord.z, dimensionalCoord.getWorld());
    }

    public BlockPos getOffSet(ForgeDirection face) {
        return new BlockPos(this.x + face.offsetX, this.y + face.offsetY, this.z + face.offsetZ, this.w);
    }

    public DimensionalCoord getDimensionalCoord() {
        return new DimensionalCoord(this.w, this.x, this.y, this.z);
    }

    public TileEntity getTileEntity() {
        if (w != null) {
            return w.getTileEntity(x, y, z);
        }
        return null;
    }

    public Block getBlock() {
        if (w != null) {
            return w.getBlock(x, y, z);
        }
        return null;
    }

    public ItemStack getPickBlock(MovingObjectPosition target, World world, EntityPlayer player) {
        if (this.getBlock() != null) {
            return this.getBlock()
                .getPickBlock(target, world, this.x, this.y, this.z, player);
        }
        return null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return w;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos(int x1, int y1, int z1, World w1)) {
            return x1 == x && y1 == y && z1 == z && Objects.equals(w1, w);
        }
        return false;
    }
}
