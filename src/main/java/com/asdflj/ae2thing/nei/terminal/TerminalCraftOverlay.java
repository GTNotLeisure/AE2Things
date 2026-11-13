package com.asdflj.ae2thing.nei.terminal;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.nei.NEI_TH_Config;
import com.asdflj.ae2thing.nei.OverlayStacks;
import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.container.slot.SlotCraftingTerm;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import codechicken.nei.FastTransferManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.ServerSync;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton.ItemOverlayState;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

@SideOnly(Side.CLIENT)
public class TerminalCraftOverlay implements IOverlayHandler {

    private static final int MAX_CRAFTS_PER_REQUEST = 64;
    private static final long SYNC_TIMEOUT_MS = 5000L;
    private static final long NO_SYNC_DELAY_MS = 150L;

    private static final Object2ObjectOpenHashMap<IOverlayHandler, TerminalCraftOverlay> WRAPPERS = new Object2ObjectOpenHashMap<>();
    private static final Object2IntOpenHashMap<Class<?>> OUTPUT_SLOT_INDEX = new Object2IntOpenHashMap<>();

    static {
        OUTPUT_SLOT_INDEX.defaultReturnValue(-1);
    }

    private final IOverlayHandler delegate;

    private TerminalCraftOverlay(IOverlayHandler delegate) {
        this.delegate = delegate;
    }

    public static synchronized IOverlayHandler wrap(IOverlayHandler delegate) {
        if (delegate == null || delegate instanceof TerminalCraftOverlay) return delegate;
        return WRAPPERS.computeIfAbsent(delegate, TerminalCraftOverlay::new);
    }

    public static boolean isWrapped(IOverlayHandler handler) {
        return handler instanceof TerminalCraftOverlay;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSupportedTerminal(GuiContainer gui) {
        if (gui == null || gui.inventorySlots == null || !(gui instanceof AEBaseGui)) return false;
        if (!NEIClientConfig.isLoaded()) return false;
        if (!NEI_TH_Config.getConfigValue(ButtonConstants.TERMINAL_AUTO_CRAFT)) return false;
        if (!(gui.inventorySlots instanceof IContainerCraftingPacket packet) || !packet.useRealItems()) return false;
        return getOutputSlot(gui) != null;
    }

    public static synchronized SlotCraftingTerm getOutputSlot(GuiContainer gui) {
        if (gui == null || gui.inventorySlots == null) return null;
        final Container container = gui.inventorySlots;
        final Class<?> type = container.getClass();
        final int cached = OUTPUT_SLOT_INDEX.getInt(type);

        if (cached >= 0 && cached < container.inventorySlots.size()
            && container.inventorySlots.get(cached) instanceof SlotCraftingTerm term) {
            return term;
        }

        for (int i = 0; i < container.inventorySlots.size(); i++) {
            if (container.inventorySlots.get(i) instanceof SlotCraftingTerm term) {
                OUTPUT_SLOT_INDEX.put(type, i);
                return term;
            }
        }
        return null;
    }

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        this.delegate.overlayRecipe(firstGui, recipe, recipeIndex, maxTransfer);
    }

    @Override
    public List<ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        return this.delegate.presenceOverlay(firstGui, recipe, recipeIndex);
    }

    @Override
    public boolean canFillCraftingGrid(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        return this.delegate.canFillCraftingGrid(firstGui, recipe, recipeIndex);
    }

    @Override
    public boolean requireShiftForOverlayRecipe() {
        return this.delegate.requireShiftForOverlayRecipe();
    }

    @Override
    public int transferRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        this.delegate.overlayRecipe(firstGui, recipe, recipeIndex, false);
        return 1;
    }

    @Override
    public boolean canCraft(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        try {
            if (getOutputSlot(firstGui) == null) return false;
            if (!this.delegate.canFillCraftingGrid(firstGui, recipe, recipeIndex)) return false;

            final IItemList<IAEStack<?>> terminal = OverlayStacks.listFor(firstGui);
            for (ItemOverlayState state : presenceOverlay(firstGui, recipe, recipeIndex)) {
                if (state.isPresent()) continue;
                if (terminal == null || !inTerminal(terminal, state.getSlot())) return false;
            }
            return true;
        } catch (Exception | Error e) {
            AELog.error(e, "[AE2Thing]: failed to check NEI auto crafting recipe " + recipe.getRecipeName());
            return false;
        }
    }

    private static boolean inTerminal(IItemList<IAEStack<?>> terminal, PositionedStack stack) {
        if (stack == null || stack.items == null) return false;
        for (ItemStack permutation : stack.items) {
            final IAEStack<?> request = Util.toAEStack(permutation);
            if (request == null) continue;
            final IAEStack<?> found = terminal.findPrecise(request);
            if (found != null && found.getStackSize() > 0) return true;
        }
        return false;
    }

    @Override
    public boolean craft(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        try {
            return craftRecipe(firstGui, recipe, recipeIndex, multiplier);
        } catch (Exception | Error e) {
            AELog.error(e, "[AE2Thing]: NEI auto crafting failed for recipe " + recipe.getRecipeName());
            return false;
        }
    }

    private boolean craftRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        final SlotCraftingTerm output = getOutputSlot(firstGui);
        if (output == null) return false;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.thePlayer.inventory.getItemStack() != null) {
            FastTransferManager.dropHeldItem(firstGui);
            if (!awaitServer(firstGui)) return false;
        }

        int budget = multiplier <= 0 ? MAX_CRAFTS_PER_REQUEST : Math.min(MAX_CRAFTS_PER_REQUEST, multiplier);
        boolean crafted = false;
        boolean filled = false;

        while (budget > 0 && isScreenAlive(firstGui)) {
            if (!filled) {
                if (!isGridReady(output, recipe, recipeIndex)) {
                    this.delegate.overlayRecipe(firstGui, recipe, recipeIndex, false);
                    if (!awaitServer(firstGui)) break;
                    if (!isGridReady(output, recipe, recipeIndex)) break;
                }
                filled = true;
            }

            final ItemStack result = output.getStack();
            if (result == null || result.stackSize <= 0) break;

            final int perCraft = result.stackSize;
            final int shiftBatch = Math.max(1, result.getMaxStackSize() / perCraft);

            if (budget >= shiftBatch) {
                final int before = countInPlayerInventory(result);
                sendAction(InventoryAction.CRAFT_SHIFT, output.slotNumber);
                if (!awaitServer(firstGui)) break;

                final int produced = countInPlayerInventory(result) - before;
                if (produced <= 0) break;
                budget -= Math.max(1, produced / perCraft);
            } else {
                if (mc.thePlayer == null || mc.thePlayer.inventory.getItemStack() != null) break;

                sendAction(InventoryAction.CRAFT_ITEM, output.slotNumber);
                if (!awaitServer(firstGui)) break;
                if (mc.thePlayer.inventory.getItemStack() == null) break;

                FastTransferManager.dropHeldItem(firstGui);
                if (!awaitServer(firstGui)) break;
                budget--;
            }

            crafted = true;
        }

        return crafted;
    }

    private static void sendAction(InventoryAction action, int slot) {
        NetworkHandler.instance.sendToServer(new PacketInventoryAction(action, slot, 0L));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isGridReady(SlotCraftingTerm output, IRecipeHandler recipe, int recipeIndex) {
        final ItemStack current = output.getStack();
        if (current == null) return false;

        final PositionedStack expected;
        try {
            expected = recipe.getResultStack(recipeIndex);
        } catch (Throwable ignored) {
            return false;
        }
        if (expected == null || expected.items == null) return false;

        for (ItemStack candidate : expected.items) {
            if (candidate != null && NEIServerUtils.areStacksSameTypeCraftingWithNBT(candidate, current)) return true;
        }
        return false;
    }

    private static boolean isScreenAlive(GuiContainer gui) {
        return Minecraft.getMinecraft().currentScreen == gui;
    }

    private static int countInPlayerInventory(ItemStack result) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || result == null) return 0;
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && NEIServerUtils.areStacksSameTypeCraftingWithNBT(stack, result)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean awaitServer(GuiContainer gui) {
        final long deadline = System.currentTimeMillis() + SYNC_TIMEOUT_MS;

        if (!NEIClientConfig.hasSMPCounterPart()) {
            if (!sleep(NO_SYNC_DELAY_MS, gui)) return false;
            return isScreenAlive(gui);
        }

        ServerSync.request();
        while (ServerSync.pending()) {
            if (!isAlive(gui, deadline)) return false;
            if (!sleep(1L, gui)) return false;
        }
        return isScreenAlive(gui);
    }

    private static boolean isAlive(GuiContainer gui, long deadline) {
        return isScreenAlive(gui) && System.currentTimeMillis() <= deadline;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean sleep(long millis, GuiContainer gui) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            return false;
        }
        return isScreenAlive(gui);
    }
}
