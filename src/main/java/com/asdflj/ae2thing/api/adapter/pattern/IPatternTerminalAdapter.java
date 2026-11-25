package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.helpers.IContainerCraftingPacket;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEFluidStack;
import codechicken.nei.item.ItemFluidDisplay;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface IPatternTerminalAdapter {

    Object2ObjectMap<Class<? extends Container>, Object2ObjectMap<String, IRecipeHandler>> map = new Object2ObjectOpenHashMap<>();

    default boolean supportFluid() {
        return false;
    }

    Class<? extends Container> getContainer();

    default void transferPack(ObjectList<OrderStack<?>> packs, IInventory inv) {
        for (OrderStack<?> stack : packs) {
            if (stack != null) {
                int index = stack.getIndex();
                ItemStack stack1;
                final Object t = stack.getStack();
                if (t instanceof ItemStack is) {
                    stack1 = is.copy();
                } else if (supportFluid() && t instanceof FluidStack fs) {
                    stack1 = ItemFluidDisplay.createStack(fs);
                } else {
                    AELog.warn(new UnsupportedOperationException("Trying to get an unsupported item!"));
                    continue;
                }
                if (index < inv.getSizeInventory()) inv.setInventorySlotContents(index, stack1);
            }
        }
    }

    default void transferPackAE(ObjectList<OrderStack<?>> packs, IAEStackInventory inv) {
        if (inv == null) return;
        for (OrderStack<?> stack : packs) {
            if (stack == null) continue;
            final int index = stack.getIndex();
            if (index < 0 || index >= inv.getSizeInventory()) continue;
            IAEStack<?> ae = null;
            final Object t = stack.getStack();
            if (t instanceof IAEStack<?>aes) {
                ae = aes;
            } else if (t instanceof ItemStack item) {
                ae = Util.toPatternStack(item.copy());
            } else if (supportFluid() && t instanceof FluidStack fluid) {
                ae = AEFluidStack.create(fluid);
            }
            if (ae != null) {
                inv.putAEStackInSlot(index, ae);
            }
        }
    }

    default IInventory getInventoryByName(Container container, String name) {
        if (container instanceof IContainerCraftingPacket c) {
            return c.getInventoryByName(name);
        }
        return null;
    }

    default String getCraftingInvName() {
        return Constants.CRAFTING;
    }

    default String getOutputInvName() {
        return Constants.OUTPUT;
    }

    default void transfer(Container container, ObjectList<OrderStack<?>> inputs, ObjectList<OrderStack<?>> outputs,
        String identifier, CPacketTransferRecipe message) {
        IRecipeHandler handler = getIdentifiers().get(identifier);
        if (handler == null) return;
        handler.transferPack(container, inputs, outputs, identifier, this, message);
    }

    default IPatternTerminalAdapter registerIdentifier(String identifier, IRecipeHandler transferPack) {
        this.getIdentifiers()
            .put(identifier, transferPack);
        return this;
    }

    default Object2ObjectMap<String, IRecipeHandler> getIdentifiers() {
        Object2ObjectMap<String, IRecipeHandler> m = map.getOrDefault(getContainer(), new Object2ObjectOpenHashMap<>());
        map.put(getContainer(), m);
        return m;
    }

}
