package com.asdflj.ae2thing.nei;

import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigList;
import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigValue;
import static com.asdflj.ae2thing.proxy.ClientProxy.mouseHandlers;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.GuiBaseInterfaceWireless;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.widget.DualPatternSlot;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.NEIRecipeUtil;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.proxy.ClientProxy;
import com.asdflj.ae2thing.util.GTUtil;
import com.asdflj.ae2thing.util.PHUtil;
import com.asdflj.ae2thing.util.enhancedsearch.EnhancedSearch;
import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.client.gui.AEBaseGui;
import appeng.container.slot.SlotFake;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.StackInfo;
import gregtech.nei.GTNEIDefaultHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class PatternTerminalRecipeTransferHandler implements IOverlayHandler {

    public static final PatternTerminalRecipeTransferHandler INSTANCE = new PatternTerminalRecipeTransferHandler();

    private static final int DEFAULT_MULTIPLIER = 1;
    private static final int MAX_MULTIPLIER = 64;

    private static Object findSameIngredient(ItemStack[] items, ItemStack item, FluidStack fluid,
        Constants.MouseWheel wheel, boolean skipBoards) {
        for (int i = 0; i < items.length; i++) {
            if (fluid != null) {
                final FluidStack fluidInfo = StackInfo.getFluid(items[i]);
                if (fluidInfo == null || !fluid.isFluidEqual(fluidInfo)) continue;
                return StackInfo.getFluid(items[Math.floorMod(i + wheel.direction, items.length)]);
            }
            if (!Platform.isSameItemPrecise(item, items[i])) continue;
            for (int step = 1; step <= items.length; step++) {
                final ItemStack picked = items[Math.floorMod(i + step * wheel.direction, items.length)];
                if (skipBoards && GTUtil.isUniversalCircuitBoard(picked)) continue;
                return picked;
            }
            return null;
        }
        return null;
    }

    public PatternTerminalRecipeTransferHandler() {
        mouseHandlers.add((event, overlayButton) -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof AEBaseGui g && overlayButton != null && GuiScreen.isShiftKeyDown()) {
                GuiOverlayButton btn = ClientProxy.getOverlayButton();
                if (btn == null) return false;
                ItemStack slotItem = null;
                if (g.theSlot instanceof SlotFake slot) {
                    slotItem = slot.getStack();
                } else if (g instanceof GuiWirelessDualInterfaceTerminal gui
                    && gui.getVirtualMESlotUnderMouse() instanceof DualPatternSlot virtualSlot
                    && virtualSlot.getAEStack() != null) {
                        slotItem = virtualSlot.getAEStack()
                            .getItemStackForNEI();
                    }
                if (slotItem != null) {
                    final FluidStack slotFluid = StackInfo.getFluid(slotItem);
                    final boolean skipBoards = NEIUtils
                        .isCircuitToBoardRecipe(btn.handlerRef.handler, btn.handlerRef.recipeIndex);
                    List<PositionedStack> list = btn.handlerRef.handler.getIngredientStacks(btn.handlerRef.recipeIndex);
                    for (PositionedStack stack : list) {
                        Object result = findSameIngredient(
                            stack.items,
                            slotItem,
                            slotFluid,
                            event.scrollAmount == -1 ? Constants.MouseWheel.NEXT : Constants.MouseWheel.PREVIEW,
                            skipBoards);
                        if (result != null) {
                            ObjectList<OrderStack<?>> in = new ObjectArrayList<>();
                            ObjectList<OrderStack<?>> out = new ObjectArrayList<>();
                            if (result instanceof FluidStack fluidOut) {
                                in.add(new OrderStack<>(slotFluid, 0));
                                out.add(new OrderStack<>(fluidOut, 0));
                            } else if (result instanceof ItemStack itemOut) {
                                in.add(new OrderStack<>(slotItem, 0));
                                out.add(new OrderStack<>(itemOut, 0));
                            }
                            AE2Thing.proxy.netHandler.sendToServer(
                                new CPacketTransferRecipe(
                                    in,
                                    out,
                                    NEIRecipeUtil.shouldCraft(btn.handlerRef.handler),
                                    isShiftKeyDown(),
                                    Constants.NEI_MOUSE_WHEEL));
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        this.transferRecipe(firstGui, recipe, recipeIndex, shift ? MAX_MULTIPLIER : DEFAULT_MULTIPLIER);
    }

    @Override
    public int transferRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        multiplier = Math.clamp(multiplier, DEFAULT_MULTIPLIER, MAX_MULTIPLIER);

        if (firstGui instanceof GuiInfusionPatternTerminal) {
            ObjectList<OrderStack<?>> in = NEIRecipeUtil.getPackageInputs(recipe, recipeIndex, false);
            ObjectList<OrderStack<?>> out = NEIRecipeUtil.getPackageOutputs(recipe, recipeIndex, false);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTransferRecipe(out, in, true, isShiftKeyDown()));
            if (!in.isEmpty() || !out.isEmpty()) {
                return multiplier;
            }
        } else if (firstGui instanceof GuiWirelessDualInterfaceTerminal gui) {
            boolean priority = gui.container.prioritize;
            boolean craft = NEIRecipeUtil.shouldCraft(recipe);
            ObjectList<OrderStack<?>> in = NEIRecipeUtil.getPackageInputs(recipe, recipeIndex, !craft && priority);
            ObjectList<OrderStack<?>> out = NEIRecipeUtil
                .getPackageOutputs(recipe, recipeIndex, !NEIRecipeUtil.notUseOther(recipe));
            final String realIdentifier = NEEUtils.realIdentifierOf(recipe);
            setSuggestion(craft, recipe, gui, in);
            if (Mods.PROGRAMMABLE_HATCHES.isModLoaded() && !craft) {
                in = PHUtil.transfer(in);
            }
            in = NEIUtils.prepareInputs(
                in,
                getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_INCLUDE_NON_CONSUMED),
                recipe,
                recipeIndex);

            boolean autoEncode = false;
            NBTTagCompound target = null;
            int targetSlot = -1;
            if (getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_AUTO_UPLOAD)) {
                List<GuiBaseInterfaceWireless.InterfaceWirelessEntryWrapper> candidates = gui
                    .collectUploadCandidates(recipe, craft);
                if (!candidates.isEmpty()) {
                    ClientProxy.setInterfaceHighlightEntry(candidates.getFirst());
                    gui.scrollHighlightIntoView();
                    autoEncode = true;
                    if (candidates.size() == 1) {
                        target = candidates.getFirst()
                            .getDimensionalCoordSide();
                        targetSlot = candidates.getFirst().slot;
                    }
                }
            }

            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketTransferRecipe(
                    in,
                    out,
                    craft,
                    isShiftKeyDown(),
                    Constants.NEI_DEFAULT,
                    autoEncode,
                    target,
                    targetSlot).withMultiplier(craft ? DEFAULT_MULTIPLIER : multiplier)
                        .withRealRecipe(realIdentifier, mergeAllowed(recipe, recipeIndex, realIdentifier)));

            if (!in.isEmpty() || !out.isEmpty()) {
                return multiplier;
            }
        }

        return 0;
    }

    private static boolean mergeAllowed(IRecipeHandler recipe, int recipeIndex, String realIdentifier) {
        if (realIdentifier != null && (NEIRecipeUtil.noMergeSet.contains(realIdentifier)
            || getConfigList(ButtonConstants.DUAL_INTERFACE_TERMINAL_NO_MERGE).contains(realIdentifier))) {
            return false;
        }
        if (!Mods.NOT_ENOUGH_ENERGISTICS.isModLoaded()) return true;
        return NEEUtils.mergeStacks(recipe, recipeIndex, realIdentifier);
    }

    private void setSuggestion(boolean craft, IRecipeHandler recipe, GuiWirelessDualInterfaceTerminal gui,
        ObjectList<OrderStack<?>> in) {
        gui.setUploadContext(recipe, craft);
        String suggestion = "";
        if (craft) {
            Optional<ItemStack> molecular = AEApi.instance()
                .definitions()
                .blocks()
                .molecularAssembler()
                .maybeStack(1);
            if (molecular.isPresent()) {
                suggestion = Platform.getItemDisplayName(molecular.get());
            }
        } else {
            final boolean enhancedSearch = getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_ENHANCED_SEARCH);
            if (Mods.isGt5Loaded() && recipe instanceof GTNEIDefaultHandler) {
                suggestion = GTUtil.getRecipeName(recipe, in, enhancedSearch);
                if (enhancedSearch) {
                    suggestion = EnhancedSearch.validatedQuery(suggestion);
                }
            }
            if (enhancedSearch && (suggestion == null || suggestion.isEmpty())) {
                suggestion = EnhancedSearch.poolQuery(recipe);
            }
            final String tabName = EnhancedSearch.recipeTabName(recipe);
            if ((suggestion == null || suggestion.isEmpty()) && tabName != null) suggestion = tabName;
        }
        if (getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL)) {
            gui.setSearchFieldText(suggestion);
            gui.setHighlightSlot();
        } else {
            gui.setSearchFieldSuggestion(suggestion);
        }

        if (getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL_AUTO_UPLOAD)) {
            gui.setSearchFieldText(suggestion);
        }
    }
}
