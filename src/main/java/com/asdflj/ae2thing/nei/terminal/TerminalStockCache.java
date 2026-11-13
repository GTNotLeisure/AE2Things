package com.asdflj.ae2thing.nei.terminal;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.nei.OverlayStacks;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import codechicken.nei.ItemStackAmount;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TerminalStockCache {

    private static final long TTL_MS = 250L;

    private static GuiContainer cachedGui;
    private static ItemStackAmount cachedStock;
    private static long cachedAt;

    private TerminalStockCache() {}

    public static synchronized ItemStackAmount get(GuiContainer gui) {
        if (gui == null) return null;

        final long now = System.currentTimeMillis();
        if (cachedStock != null && gui == cachedGui && now - cachedAt < TTL_MS) return cachedStock;

        final IItemList<IAEStack<?>> list = OverlayStacks.listFor(gui);
        if (list == null) return null;

        final ItemStackAmount stock = new ItemStackAmount();
        for (IAEStack<?> stack : list) {
            if (stack == null || stack.getStackSize() <= 0) continue;
            final ItemStack item = stack.getItemStackForNEI();
            if (item == null) continue;
            stock.add(item, stack.getStackSize());
        }

        cachedGui = gui;
        cachedStock = stock;
        cachedAt = now;
        return stock;
    }
}
