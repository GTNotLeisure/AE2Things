package com.asdflj.ae2thing.api.container;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.util.Util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;

public final class FluidContainerProvider implements IEmptyContainerProvider {

    public static final FluidContainerProvider INSTANCE = new FluidContainerProvider();

    private static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    private static final ItemStack GLASS_BOTTLE = new ItemStack(Items.glass_bottle, 1);

    private ItemStack defaultContainer;

    private FluidContainerProvider() {}

    public void setDefaultContainer(@Nullable ItemStack container) {
        this.defaultContainer = container == null ? null : container.copy();
    }

    @Nullable
    @Override
    public ItemStack create(IAEStack<?> target) {
        if (!(target instanceof IAEFluidStack fluid) || fluid.getFluidStack() == null) return null;
        final FluidStack fs = fluid.getFluidStack();
        if (canFill(BUCKET, fs)) return BUCKET.copy();
        if (defaultContainer != null && canFill(defaultContainer, fs)) return defaultContainer.copy();
        return GLASS_BOTTLE.copy();
    }

    private static boolean canFill(ItemStack container, FluidStack fluid) {
        final MutablePair<Integer, ItemStack> result = Util.FluidUtil.fillStack(container.copy(), fluid);
        return result != null && result.left != 0;
    }
}
