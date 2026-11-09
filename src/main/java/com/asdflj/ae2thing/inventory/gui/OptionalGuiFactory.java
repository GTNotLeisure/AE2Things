package com.asdflj.ae2thing.inventory.gui;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class OptionalGuiFactory implements IGuiFactory {

    private final BooleanSupplier available;
    private final Supplier<IGuiFactory> factory;

    OptionalGuiFactory(BooleanSupplier available, Supplier<IGuiFactory> factory) {
        this.available = available;
        this.factory = factory;
    }

    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        IGuiFactory delegate = getDelegate();
        return delegate == null ? null : delegate.createServerGui(player, world, x, y, z, face);
    }

    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        IGuiFactory delegate = getDelegate();
        return delegate == null ? null : delegate.createClientGui(player, world, x, y, z, face);
    }

    private IGuiFactory getDelegate() {
        return available.getAsBoolean() ? factory.get() : null;
    }
}
