package com.asdflj.ae2thing.network;

import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.common.Config;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketServerConfig implements IMessage {

    private NBTTagCompound values;

    @SuppressWarnings("unused")
    public SPacketServerConfig() {}

    public SPacketServerConfig(NBTTagCompound values) {
        this.values = values;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        values = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, values);
    }

    public static class Handler implements IMessageHandler<SPacketServerConfig, IMessage> {

        @Override
        public IMessage onMessage(SPacketServerConfig message, MessageContext ctx) {
            Config.SERVER_SETTINGS.apply(message.values);
            return null;
        }
    }
}
