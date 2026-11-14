package com.asdflj.ae2thing.network;

import static appeng.api.networking.crafting.CraftingItemList.ACTIVE;

import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.client.gui.container.AE2ThingPrimaryGui;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternModifier;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueAmount;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.IPatternValueContainer;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.CPUCraftingPreview;
import com.asdflj.ae2thing.util.InvUtil;
import com.asdflj.ae2thing.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CPacketInventoryActionExtend implements IMessage {

    public static final int PICK_INTO_EMPTY_SLOT = 0;
    public static final int REFILL_HELD_STACK = 1;

    private InventoryActionExtend action;
    private int slot;
    private long id;
    private IAEStack<?> stack;
    private boolean isEmpty;

    @SuppressWarnings("unused")
    public CPacketInventoryActionExtend() {}

    @SuppressWarnings("unused")
    public CPacketInventoryActionExtend(final InventoryActionExtend action, final int slot, final int id) {
        this(action, slot, id, null);
    }

    public CPacketInventoryActionExtend(final InventoryActionExtend action) {
        this(action, 0, 0, null);
    }

    public CPacketInventoryActionExtend(final InventoryActionExtend action, final int slot, final int id,
        IAEStack<?> stack) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = stack;
        this.isEmpty = stack == null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeInt(slot);
        buf.writeLong(id);
        buf.writeBoolean(isEmpty);
        if (!isEmpty) {
            try {
                IAEStack.writeToPacketGeneric(buf, stack);
            } catch (IOException e) {
                throw new EncoderException("Failed to encode extended inventory action stack", e);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = PacketDecodeUtil.readIntEnum(buf, InventoryActionExtend.values(), "extended inventory action");
        slot = buf.readInt();
        id = buf.readLong();
        isEmpty = buf.readBoolean();
        if (!isEmpty) {
            try {
                stack = IAEStack.fromPacketGeneric(buf);
            } catch (IOException e) {
                throw new DecoderException("Failed to decode extended inventory action stack", e);
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketInventoryActionExtend, IMessage> {

        private void extractItemFromME(EntityPlayerMP player, IAEItemStack requestItem, int slot, int limit) {
            ItemStack expected = requestItem.getItemStack();
            limit = Math.min(limit, InvUtil.getMainSlotMissingCount(player, expected, slot));
            if (limit <= 0) return;
            int remaining = limit;

            for (int sourceSlot : InvUtil.getSlotRefs(
                player,
                EnumSet.of(PlayerInvType.PLAYER_INV, PlayerInvType.PLAYER_BAUBLES, PlayerInvType.YOG_STORAGE))) {
                if (remaining <= 0) break;

                try {
                    ItemStack terminal = InvUtil.readSlotRef(player, sourceSlot);
                    if (terminal == null || terminal.getItem() == null) continue;

                    IWirelessTermHandler handler = AEApi.instance()
                        .registries()
                        .wireless()
                        .getWirelessTerminalHandler(terminal);
                    if (handler == null) continue;

                    WirelessObject object = new WirelessObject(terminal, player.worldObj, sourceSlot, 0, 0, player);
                    IAEItemStack request = requestItem.copy();
                    request.setStackSize(remaining);
                    IAEItemStack extracted = object.getItemInventory()
                        .extractItems(request, Actionable.MODULATE, object.getSource());
                    if (extracted == null || extracted.getStackSize() <= 0) continue;

                    ItemStack received = extracted.getItemStack();
                    ItemStack destination = player.inventory.getStackInSlot(slot);
                    if (destination == null) {
                        player.inventory.setInventorySlotContents(slot, received);
                    } else {
                        destination.stackSize += received.stackSize;
                    }

                    remaining -= received.stackSize;
                    player.inventory.markDirty();
                } catch (AppEngException e) {
                    AELog.debug("[AE2Thing] pick-block: terminal slot %s unavailable: %s", sourceSlot, e.getMessage());
                } catch (Exception e) {
                    AELog.warn("[AE2Thing] pick-block: failed at terminal slot %s", sourceSlot);
                    AELog.error(e);
                }
            }

            if (remaining < limit) {
                player.inventoryContainer.detectAndSendChanges();
            } else {
                AELog.debug("AE2Things pick-block: no items extracted into hotbar slot %s", slot);
            }
        }

        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryActionExtend message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if (message.action == InventoryActionExtend.REQUEST_ITEM) {
                if (!sender.isEntityAlive() || sender.capabilities.isCreativeMode || message.slot < 0 || message.slot >= 9 || message.slot != sender.inventory.currentItem || !(message.stack instanceof IAEItemStack request)) {
                    return null;
                }
                ItemStack requested = request.getItemStack();
                if (requested == null || requested.getItem() == null) return null;

                ItemStack held = sender.inventory.getStackInSlot(message.slot);
                final ItemStack target;

                if (message.id == REFILL_HELD_STACK) {
                    if (held == null || held.stackSize <= 0 || !Util.sameStackType(held, requested)) return null;
                    target = held.copy();
                } else if (message.id == PICK_INTO_EMPTY_SLOT) {
                    if (held != null) return null;
                    target = requested.copy();
                } else {
                    return null;
                }

                int missing = InvUtil.getMainSlotMissingCount(sender, target, message.slot);
                if (missing <= 0) return null;

                int moved = InvUtil.refillMainSlotFromInv(sender, target, message.slot);
                if (moved > 0) {
                    sender.inventory.markDirty();
                    sender.inventoryContainer.detectAndSendChanges();
                    return null;
                }

                extractItemFromME(sender, request, message.slot, missing);
                return null;
            }
            if(sender.openContainer instanceof ContainerCraftingTerminal) {
                return null;
            }
            if (sender.openContainer instanceof final AEBaseContainer baseContainer) {
                Object target = baseContainer.getTarget();
                if (message.action == InventoryActionExtend.SET_PATTERN_NAME) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null && message.stack != null) {
                        final ForgeDirection side = Objects.requireNonNull(context.getSide());
                        final TileEntity te = context.getTile();
                        final BlockPos pos;
                        if (te != null) {
                            pos = new BlockPos(te);
                            InventoryHandler
                                .openGui(sender, te.getWorldObj(), pos, side, GuiType.PATTERN_NAME_SET);
                        } else if (target instanceof WirelessTerminal wirelessTerminal) {
                            pos = new BlockPos(wirelessTerminal.getX(), 0, 0);
                            InventoryHandler.openItemGui(
                                sender,
                                sender.getEntityWorld(),
                                wirelessTerminal.getX(),
                                side,
                                GuiType.PATTERN_NAME_SET_ITEM);
                        } else {
                            return null;
                        }
                        if (sender.openContainer instanceof final ContainerPatternValueAmount cpv) {
                            final StorageName storage;
                            final int index;
                            if (message.id >= 0 && message.id < StorageName.values().length) {
                                storage = StorageName.values()[(int) message.id];
                                index = message.slot;
                            } else {
                                final Slot clickedSlot = baseContainer.getSlot(message.slot);
                                storage = baseContainer instanceof IPatternValueContainer owner
                                    ? owner.getAEStorageName(clickedSlot)
                                    : null;
                                index = clickedSlot == null ? -1 : clickedSlot.getSlotIndex();
                            }
                            final GuiType originGui = getOriginGui(baseContainer);
                            if (storage == null || index < 0 || originGui == null) {
                                return null;
                            }
                            cpv.setPrimaryGui(
                                AE2ThingPrimaryGui
                                    .create(originGui, getOriginGuiIcon(originGui), pos, side));
                            cpv.updateVirtualSlot(storage, index, message.stack);
                            cpv.detectAndSendChanges();
                        }
                    }
                } else if (message.action == InventoryActionExtend.GET_CRAFTING_STATE) {
                    if(target instanceof IActionHost gh){
                        if (gh.getActionableNode() == null || gh.getActionableNode().getGrid() == null) {
                            return null;
                        }
                        ICraftingGrid craftingGrid = gh.getActionableNode().getGrid().getCache(ICraftingGrid.class);
                        NBTTagCompound cpuData = new NBTTagCompound();
                        NBTTagList tagList = new NBTTagList();
                        cpuData.setTag(Constants.CPU_LIST,tagList);
                        int i = 0;
                        if (craftingGrid == null || message.stack == null) {
                            return null;
                        }
                        for (ICraftingCPU cpu: craftingGrid.getCpus()) {
                            i++;
                            if(cpu instanceof CraftingCPUCluster ccc && ccc.getFinalMultiOutput() != null){
                                if(message.stack.isSameType(ccc.getFinalMultiOutput())){
                                    IItemList<IAEStack<?>> list =  AEApi.instance().storage().createAEStackList();
                                    ccc.getModernListOfItem(list, ACTIVE);
                                    List<IAEStack<?>> activeItems = getActiveCraftingItems(list);
                                    if(activeItems.isEmpty()){
                                        continue;
                                    }
                                    NBTTagCompound data = new NBTTagCompound();
                                    final String name;
                                    if(ccc.getName().isEmpty()){
                                        name = GuiText.CPUs.getLocal() + ": #" + i;
                                    }else{
                                        name = GuiText.CPUs.getLocal() + ": "  + ccc.getName().substring(0, Math.min(20, ccc.getName().length()));
                                    }
                                    new CPUCraftingPreview(name, ccc.getRemainingItemCount(), ccc.getElapsedTime(), activeItems).writeToNBT(data);
                                    tagList.appendTag(data);
                                }
                            }
                        }
                        AE2Thing.proxy.netHandler.sendTo(new SPacketCraftingStateUpdate(cpuData), ctx.getServerHandler().playerEntity);
                    }
                } else if (message.action == InventoryActionExtend.CLEAR_PATTERN && baseContainer instanceof ContainerPatternModifier patternModifier) {
                    patternModifier.clearPattern();
                } else if (message.action == InventoryActionExtend.REPLACE_PATTERN && baseContainer instanceof ContainerPatternModifier patternModifier) {
                    patternModifier.replacePattern();
                } else if (message.action == InventoryActionExtend.SET_PATTERN_VALUE) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null && message.stack != null) {
                        final ForgeDirection side = Objects.requireNonNull(context.getSide());
                        final TileEntity te = context.getTile();
                        final BlockPos pos;
                        if (te != null) {
                            pos = new BlockPos(te);
                            InventoryHandler
                                .openGui(sender, te.getWorldObj(), pos, side, GuiType.PATTERN_VALUE_SET);
                        } else if (target instanceof IInventorySlotAware slotAware) {
                            pos = new BlockPos(slotAware.getInventorySlot(), 0, 0);
                            InventoryHandler
                                .openGui(sender, sender.getEntityWorld(), pos, side, GuiType.PATTERN_VALUE_SET_ITEM);
                        } else {
                            return null;
                        }
                        if (sender.openContainer instanceof final ContainerPatternValueAmount cpv) {
                            final StorageName storage;
                            final int index;
                            if (message.id >= 0 && message.id < StorageName.values().length) {
                                storage = StorageName.values()[(int) message.id];
                                index = message.slot;
                            } else {
                                final Slot clickedSlot = baseContainer.getSlot(message.slot);
                                storage = baseContainer instanceof IPatternValueContainer owner
                                    ? owner.getAEStorageName(clickedSlot)
                                    : null;
                                index = clickedSlot == null ? -1 : clickedSlot.getSlotIndex();
                            }
                            final GuiType originGui = getOriginGui(baseContainer);
                            if (storage == null || index < 0 || originGui == null) {
                                return null;
                            }
                            cpv.setPrimaryGui(
                                AE2ThingPrimaryGui
                                    .create(originGui, getOriginGuiIcon(originGui), pos, side));
                            cpv.updateVirtualSlot(storage, index, message.stack);
                            cpv.detectAndSendChanges();
                        }
                    }
                }
            }
            return null;
        }

        private GuiType getOriginGui(Object container) {
            if (container instanceof ContainerInfusionPatternTerminal) {
                return GuiType.INFUSION_PATTERN_TERMINAL;
            }
            if (container instanceof ContainerPatternModifier) {
                return GuiType.PATTERN_MODIFIER;
            }
            if (container instanceof ContainerWirelessDualInterfaceTerminal) {
                return GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL;
            }
            return null;
        }

        private ItemStack getOriginGuiIcon(GuiType guiType) {
            return switch (guiType) {
                case INFUSION_PATTERN_TERMINAL -> ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack();
                case PATTERN_MODIFIER -> ItemAndBlockHolder.ITEM_PATTERN_MODIFIER.stack();
                case WIRELESS_DUAL_INTERFACE_TERMINAL -> ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL
                    .stack();
                default -> null;
            };
        }

        private List<IAEStack<?>> getActiveCraftingItems(IItemList<IAEStack<?>> list) {
            List<IAEStack<?>> activeItems = new ObjectArrayList<>();
            for (IAEStack<?> item : list) {
                activeItems.add(item);
            }
            activeItems.sort(
                Comparator.comparingLong(IAEStack<?>::getStackSize)
                    .reversed());
            if (activeItems.size() <= CPUCraftingPreview.maxSize) {
                return activeItems;
            }
            return new ObjectArrayList<>(activeItems.subList(0, CPUCraftingPreview.maxSize));
        }
    }

}
