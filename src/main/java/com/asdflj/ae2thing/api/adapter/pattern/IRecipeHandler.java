package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;

import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;

import it.unimi.dsi.fastutil.objects.ObjectList;

@FunctionalInterface
public interface IRecipeHandler {

    void transferPack(Container container, ObjectList<OrderStack<?>> inputs, ObjectList<OrderStack<?>> outputs,
        String identifier, IPatternTerminalAdapter adapter, CPacketTransferRecipe message);
}
