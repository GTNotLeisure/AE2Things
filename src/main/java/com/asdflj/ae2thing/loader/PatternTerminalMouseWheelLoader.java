package com.asdflj.ae2thing.loader;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.FCPatternTerminal;
import com.asdflj.ae2thing.api.adapter.pattern.IRecipeHandler;
import com.asdflj.ae2thing.api.adapter.pattern.THDualInterfacePatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPatternTermEx;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

public class PatternTerminalMouseWheelLoader implements Runnable {

    @Override
    public void run() {
        IRecipeHandler handler = (container, inputs, outputs, _, adapter, _) -> {
            if (container instanceof IAEAppEngInventory inventory) {
                ItemStack in = (ItemStack) inputs.getFirst()
                    .getStack();
                ItemStack out = (ItemStack) outputs.getFirst()
                    .getStack();
                IInventory inv = adapter.getInventoryByName(container, adapter.getCraftingInvName());
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (Platform.isSameItemPrecise(inv.getStackInSlot(i), in)) {
                        inv.setInventorySlotContents(i, out);
                    }
                }
                container.onCraftMatrixChanged(inv);
                inventory.saveChanges();
            }
        };

        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new THDualInterfacePatternTerminal())
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, (container, inputs, outputs, _, _, _) -> {
                if (container instanceof ContainerWirelessDualInterfaceTerminal c) {
                    if (inputs == null || outputs == null || inputs.isEmpty() || outputs.isEmpty()) return;

                    final OrderStack<?> input = inputs.getFirst();
                    final OrderStack<?> output = outputs.getFirst();
                    if (input == null || output == null) return;

                    final Object in = input.getStack();
                    final Object out = output.getStack();
                    final IPatternTerminal pt = c.getContainer()
                        .getPatternTerminal();
                    if (pt == null) return;

                    if (pt.isCraftingRecipe()) {
                        if (!(in instanceof ItemStack itemIn) || !(out instanceof ItemStack itemOut)) return;
                        if (itemIn.getItem() == null || itemOut.getItem() == null) return;

                        final IInventory inv = pt.getInventoryByName(Constants.CRAFTING);
                        if (inv == null) return;
                        for (int i = 0; i < inv.getSizeInventory(); i++) {
                            if (Platform.isSameItemPrecise(inv.getStackInSlot(i), itemIn)) {
                                inv.setInventorySlotContents(i, itemOut.copy());
                            }
                        }

                        c.onCraftMatrixChanged(inv);
                        c.saveChanges();
                        return;
                    }

                    final IAEStackInventory inv = ((appeng.api.parts.IPatternTerminal) pt)
                        .getAEInventoryByName(StorageName.CRAFTING_INPUT);
                    if (inv == null) return;

                    final IAEStack<?> target;
                    if (in instanceof FluidStack fluidIn) {
                        target = AEFluidStack.create(fluidIn.copy());
                    } else if (in instanceof ItemStack itemIn && itemIn.getItem() != null) {
                        target = AEItemStack.create(itemIn.copy());
                    } else {
                        return;
                    }
                    if (target == null) return;

                    final IAEStack<?> replacement;
                    if (out instanceof FluidStack fluidOut) {
                        replacement = AEFluidStack.create(fluidOut.copy());
                    } else if (out instanceof ItemStack itemOut && itemOut.getItem() != null) {
                        replacement = Util.toPatternStack(itemOut.copy());
                    } else {
                        return;
                    }
                    if (replacement == null) return;

                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        final IAEStack<?> current = inv.getAEStackInSlot(i);
                        if (current != null && target.isSameType(current)) {
                            inv.putAEStackInSlot(i, replacement.copy());
                        }
                    }

                    c.saveChanges();
                }
            });
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new FCPatternTerminal(ContainerPatternTerm.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new FCPatternTerminal(ContainerPatternTermEx.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);

        if (Mods.THAUMIC_ENERGISTICS.isModLoaded()) {
            AE2ThingAPI.instance()
                .terminal()
                .registerPatternTerminal(() -> ContainerInfusionPatternTerminal.class)
                .registerIdentifier(Constants.NEI_MOUSE_WHEEL, (container, inputs, outputs, _, _, _) -> {
                    if (container instanceof ContainerInfusionPatternTerminal ct) {
                        ct.getPatternTerminal()
                            .setCraftingRecipe(true);
                        ct.setCrafting(true);
                        IInventory outputSlot = ct.getInventoryByName(Constants.OUTPUT);
                        ItemStack in = (ItemStack) inputs.getFirst()
                            .getStack();
                        ItemStack out = (ItemStack) outputs.getFirst()
                            .getStack();
                        for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                            if (Platform.isSameItemPrecise(outputSlot.getStackInSlot(i), in)) {
                                outputSlot.setInventorySlotContents(i, out);
                            }
                        }
                        ct.onCraftMatrixChanged(outputSlot);
                        ct.saveChanges();
                    }
                });
        }
    }

}
