package com.asdflj.ae2thing.network;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.inventory.IModernContainerPickup;

import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public class CPacketContainerInteraction implements IMessage {

    private IAEStack<?> target;
    private boolean moveToPlayer;

    @SuppressWarnings("unused")
    public CPacketContainerInteraction() {}

    public CPacketContainerInteraction(@Nullable IAEStack<?> target, boolean moveToPlayer) {
        this.target = target;
        this.moveToPlayer = moveToPlayer;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.moveToPlayer);
        buf.writeBoolean(this.target != null);
        if (this.target != null) {
            try {
                IAEStack.writeToPacketGeneric(buf, this.target);
            } catch (IOException e) {
                throw new EncoderException("Failed to encode container interaction stack", e);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.moveToPlayer = buf.readBoolean();
        if (buf.readBoolean()) {
            try {
                this.target = IAEStack.fromPacketGeneric(buf);
            } catch (IOException e) {
                throw new DecoderException("Failed to decode container interaction stack", e);
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketContainerInteraction, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketContainerInteraction message, MessageContext ctx) {
            if (!AE2ThingConfig.modernInteraction) return null;
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || message.target == null) return null;
            final Container container = player.openContainer;
            if (container instanceof IModernContainerPickup pickup) {
                pickup.fillFromNetwork(message.target, message.moveToPlayer);
            }
            return null;
        }
    }
}
