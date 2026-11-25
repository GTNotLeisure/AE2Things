package com.asdflj.ae2thing.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.inventory.IPlayerPinInventory;
import com.asdflj.ae2thing.pin.IPlayerPinActions;

import appeng.api.storage.data.IAEStack;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.items.contents.PinsHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public class CPacketPlayerPinAction implements IMessage {

    public static final int TOGGLE_STACK = 0;
    public static final int SET_EMPTY_SLOT = 1;
    public static final int TOGGLE_INVENTORY = 2;
    public static final int STORE_INVENTORY = 3;
    public static final int REMOVE_SLOT = 4;

    public static boolean isEnabled(int operation, boolean enhancedPins, boolean modernInteraction) {
        return enhancedPins && operation >= TOGGLE_STACK
            && operation <= REMOVE_SLOT
            && (operation == REMOVE_SLOT || modernInteraction);
    }

    private int windowId;
    private int operation;
    private int slotIndex;
    private IAEStack<?> key;

    public CPacketPlayerPinAction() {}

    public CPacketPlayerPinAction(int windowId, int operation, int slotIndex, IAEStack<?> key) {
        this.windowId = windowId;
        this.operation = operation;
        this.slotIndex = slotIndex;
        this.key = key == null ? null : key.copy();
        if (this.key != null) this.key.setStackSize(0);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeByte(operation);
        buf.writeInt(slotIndex);
        buf.writeBoolean(key != null);
        if (key == null) return;
        try {
            IAEStack.writeToPacketGeneric(buf, key);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        operation = buf.readUnsignedByte();
        slotIndex = buf.readInt();
        key = null;
        if (!buf.readBoolean()) return;
        try {
            key = IAEStack.fromPacketGeneric(buf);
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }

    public static class Handler implements IMessageHandler<CPacketPlayerPinAction, IMessage> {

        @Override
        public IMessage onMessage(CPacketPlayerPinAction message, MessageContext ctx) {
            if (!isEnabled(message.operation, AE2ThingConfig.enhancedPins, AE2ThingConfig.modernInteraction))
                return null;
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || !(player.openContainer instanceof ContainerMEMonitorable container)
                || container.windowId != message.windowId
                || !container.canInteractWith(player)) return null;
            PinsHandler handler = container.getPinsHandler();
            if (handler == null || handler.getPlayer() != player) return null;
            IPlayerPinActions pins = (IPlayerPinActions) handler;
            boolean emptyCursor = player.inventory.getItemStack() == null;
            switch (message.operation) {
                case TOGGLE_STACK -> {
                    if (emptyCursor && message.key != null && message.slotIndex == -1) pins.ae2t$toggle(message.key);
                }
                case SET_EMPTY_SLOT -> {
                    if (message.key != null) pins.ae2t$manualSetEmpty(message.slotIndex, message.key);
                }
                case TOGGLE_INVENTORY -> {
                    if (emptyCursor && message.key == null) {
                        pins.ae2t$toggle(
                            ((IPlayerPinInventory) container).ae2t$inventoryPinKey(player, message.slotIndex));
                    }
                }
                case STORE_INVENTORY -> {
                    if (emptyCursor && message.key == null) {
                        ((IPlayerPinInventory) container).ae2t$storeAndPin(player, message.slotIndex);
                    }
                }
                case REMOVE_SLOT -> {
                    if (emptyCursor && message.key == null) pins.ae2t$removeActive(message.slotIndex);
                }
                default -> {
                    return null;
                }
            }
            handler.update(true);
            return null;
        }
    }
}
