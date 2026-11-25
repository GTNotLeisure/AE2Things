package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.IGuiMonitorTerminal;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.network.SPacketItemGuiHint;
import com.glodblock.github.client.gui.FCGuiTextField;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCraftingTreeData;
import appeng.crafting.v2.CraftingJobV2;
import appeng.integration.modules.NEI;
import appeng.me.cache.CraftingGridCache;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.recipe.StackInfo;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;

public class Util {

    private static int AE_VERSION = -1;
    private static List<IAEStackType<?>> SORTED_TYPES;
    private static int SORTED_TYPES_SIZE = -1;

    // region information

    public static int getAEVersion() {
        if (AE_VERSION == -1) {
            Optional<ModContainer> mod = Loader.instance()
                .getActiveModList()
                .stream()
                .filter(
                    x -> x.getModId()
                        .equals("appliedenergistics2"))
                .findFirst();
            if (mod.isPresent()) {
                try {
                    AE_VERSION = Integer.parseInt(
                        mod.get()
                            .getVersion()
                            .split("-")[2]);
                } catch (Exception ignored) {
                    AE_VERSION = 0;
                }
            } else {
                AE_VERSION = 0;
            }
        }
        return AE_VERSION;
    }

    public static String getModId(IAEItemStack item) {
        if (item.getItem() instanceof ItemFluidDrop) {
            FluidStack fs = ItemFluidDrop.getFluidStack(item.getItemStack());
            if (fs == null) return GameRegistry.findUniqueIdentifierFor(item.getItem()).modId;
            return getFluidModID(fs.getFluid());
        }
        return Platform.getModId(item);
    }

    public static String getFluidModID(Fluid fluid) {
        String name = FluidRegistry.getDefaultFluidName(fluid);
        try {
            return name.split(":")[0];
        } catch (Exception e) {
            return "";
        }
    }

    @NotNull
    public static String getDisplayName(IAEItemStack item) {
        FluidStack fs = StackInfo.getFluid(item.getItemStack());
        if (fs != null) {
            return fs.getLocalizedName();
        }
        return Platform.getItemDisplayName(item);
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() instanceof IFluidContainerItem) {
                FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
            if (FluidContainerRegistry.isContainer(stack)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
        }
        return null;
    }

    // endregion
    // region Crafting jobs

    @SuppressWarnings("rawtypes")
    public static boolean replan(EntityPlayer player, ContainerCraftConfirm c) {
        if (Ae2Reflect.getJob(c) == null) {
            return false;
        }
        c.simulation = true;
        c.bytesUsed = 0;
        Object target;
        target = c.getTarget();
        if (target instanceof final IGridHost gh) {
            final IGridNode gn = gh.getGridNode(ForgeDirection.UNKNOWN);

            if (gn == null) {
                return false;
            }

            final IGrid g = gn.getGrid();
            if (g == null || c.getItemToCraft() == null) {
                return false;
            }

            Future<ICraftingJob> futureJob = null;
            try {
                final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                if (cg instanceof CraftingGridCache cgc) {
                    futureJob = cgc.beginCraftingJob(
                        c.getWorld(),
                        g,
                        c.getActionSource(),
                        c.getItemToCraft(),
                        null);
                }

                if (player.openContainer instanceof final ContainerCraftConfirm ccc) {
                    ccc.setJob(futureJob);
                    ccc.detectAndSendChanges();
                }
                return true;
            } catch (final Throwable e) {
                if (futureJob != null) {
                    futureJob.cancel(true);
                }
                AELog.debug(e);
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public static void sendCraftingTree(EntityPlayer player, ContainerCraftConfirm c) {
        ICraftingJob job = Ae2Reflect.getJob(c);
        if (job instanceof CraftingJobV2 jobV2 && player instanceof EntityPlayerMP playerMP) {
            for (PacketCraftingTreeData packet : PacketCraftingTreeData.createChunks(jobV2)) {
                NetworkHandler.instance.sendTo(packet, playerMP);
            }
            return;
        }
        player.addChatMessage(new ChatComponentTranslation("ae2thing.chat.crafting_tree_unavailable"));
    }

    // endregion
    // region stack types and conversion

    public static List<IAEStackType<?>> getTypes() {
        final Collection<IAEStackType<?>> all = AEStackTypeRegistry.getAllTypes();
        if (SORTED_TYPES == null || SORTED_TYPES_SIZE != all.size()) {
            SORTED_TYPES = Collections.unmodifiableList(AEStackTypeRegistry.getSortedTypes());
            SORTED_TYPES_SIZE = all.size();
        }
        return SORTED_TYPES;
    }

    public static IAEStack<?> toAEStack(ItemStack is) {
        if (is == null || is.getItem() == null) return null;

        for (IAEStackType<?> type : getTypes()) {
            final IAEStack<?> converted = toAEStack(type, is);
            if (converted != null) return converted;
        }
        return AEItemStack.create(is);
    }

    public static IAEStack<?> toAEStack(IAEStackType<?> type, ItemStack is) {
        if (type == null || is == null || is.getItem() == null) return null;

        final IAEStack<?> converted = type.convertStackFromItem(is);
        return converted != null ? converted : type.getStackFromContainerItem(is);
    }

    public static IAEStack<?> toPatternStack(ItemStack is) {
        if (is == null || is.getItem() == null) return null;

        final IAEStack<?> converted = tryConvertDisplayStack(is);
        return converted != null ? converted : AEItemStack.create(is);
    }

    public static IAEStack<?> toPatternStack(IAEStack<?> stack) {
        if (!(stack instanceof IAEItemStack item)) return stack;

        final ItemStack is = item.getItemStack();
        if (is == null || is.getItem() == null) return item;

        final IAEStack<?> converted = tryConvertDisplayStack(is);
        return converted != null ? converted : stack;
    }

    private static IAEStack<?> tryConvertDisplayStack(ItemStack is) {
        for (IAEStackType<?> type : getTypes()) {
            final IAEStack<?> converted = type.convertStackFromItem(is.copy());
            if (converted != null) return converted;
        }

        return null;
    }

    // endregion

    public static void setSearchFieldText(AEBaseGui gui, String text) {
        String displayName = NEI.searchField.getEscapedSearchText(text);
        if (gui instanceof IGuiMonitorTerminal gmt) {
            gmt.getSearchField()
                .setText(displayName);
            gmt.getRepo()
                .setSearchString(displayName);
            gmt.getRepo()
                .updateView();
        } else {
            IDisplayRepo repo = getDisplayRepo(gui);
            if (repo != null) {
                setSearchFieldText(gui, gui.getClass(), displayName);
                repo.setSearchString(displayName);
                repo.updateView();
            }
        }
    }

    public static IDisplayRepo getDisplayRepo(AEBaseGui gui) {
        if (gui instanceof IGuiMonitorTerminal gmt) {
            return gmt.getRepo();
        }
        return getDisplayRepo(gui, gui.getClass());
    }

    @SuppressWarnings("unchecked")
    private static void setSearchFieldText(AEBaseGui gui, Class<? extends AEBaseGui> clazz, String text) {
        try {
            if (clazz == AEBaseGui.class) {
                return;
            }
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType() == MEGuiTextField.class) {
                    f.setAccessible(true);
                    ((MEGuiTextField) f.get(gui)).setText(text);
                    return;
                } else if (f.getType() == FCGuiTextField.class) {
                    f.setAccessible(true);
                    ((FCGuiTextField) f.get(gui)).setText(text);
                    return;
                }
            }
            setSearchFieldText(gui, (Class<? extends AEBaseGui>) clazz.getSuperclass(), text);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private static IDisplayRepo getDisplayRepo(AEBaseGui gui, Class<? extends AEBaseGui> clazz) {
        try {
            if (clazz == AEBaseGui.class) {
                return null;
            }
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType() == IDisplayRepo.class || f.getType() == ItemRepo.class) {
                    f.setAccessible(true);
                    return (IDisplayRepo) f.get(gui);
                }
            }
            return getDisplayRepo(gui, (Class<? extends AEBaseGui>) clazz.getSuperclass());
        } catch (Exception ignored) {}
        return null;
    }

    public static ItemStack getGuiStack(EntityPlayer player, int x) {
        if (Platform.isClient() && PlayerInvType.typeOf(x) != PlayerInvType.PLAYER_INV) {
            final ItemStack hinted = ItemGuiHintCache.get(x);
            if (hinted != null) return hinted;
        }
        return InvUtil.readSlotRef(player, x);
    }

    public static void hintGuiStack(EntityPlayerMP player, int x) {
        if (player == null || PlayerInvType.typeOf(x) == PlayerInvType.PLAYER_INV) return;
        final ItemStack stack = InvUtil.readSlotRef(player, x);
        if (stack != null) AE2Thing.proxy.netHandler.sendTo(new SPacketItemGuiHint(x, stack), player);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean sameStackType(ItemStack first, ItemStack second) {
        return first != null && second != null
            && first.isItemEqual(second)
            && ItemStack.areItemStackTagsEqual(first, second);
    }

    public static boolean hasInfinityBoosterCard(ItemStack item) {
        return ItemStackNBT.getBoolean(item, Constants.INFINITY_BOOSTER_CARD);
    }

    public static boolean hasEnergyCard(ItemStack is) {
        return ItemStackNBT.getBoolean(is, Constants.INFINITY_ENERGY_CARD);
    }

    public static int countNotNull(Object[] objects) {
        int count = 0;
        for (Object o : objects) {
            if (o != null) {
                count++;
            }
        }
        return count;
    }

    public static boolean isSameDimensionalCoord(@Nullable DimensionalCoord a, @Nullable DimensionalCoord b) {
        return a != null && b != null && a.x == b.x && a.y == b.y && a.z == b.z && a.getDimension() == b.getDimension();
    }
}
