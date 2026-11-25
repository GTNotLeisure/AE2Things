package com.asdflj.ae2thing.api.config;

import java.io.IOException;
import java.util.Collection;

import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class PacketSyncConfig implements IMessage {

    public final Object2ObjectMap<String, String> syncedElements = new Object2ObjectOpenHashMap<>();

    public PacketSyncConfig() {}

    PacketSyncConfig(Collection<SyncedConfigElement> elements) {
        for (SyncedConfigElement element : elements) {
            try {
                syncedElements.put(element.getId(), element.getValue());
            } catch (ConfigException ex) {
                ConfigLog.error("Cannot serialize " + element.getId(), ex);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf bytes) {
        PacketBuffer buf = new PacketBuffer(bytes);
        int size = buf.readInt();
        if (size < 0 || size > buf.readableBytes() / 2)
            throw new DecoderException("Invalid config entry count: " + size);
        try {
            for (int i = 0; i < size; i++)
                syncedElements.put(buf.readStringFromBuffer(32767), buf.readStringFromBuffer(32767));
        } catch (IOException ex) {
            throw new DecoderException(ex);
        }
    }

    @Override
    public void toBytes(ByteBuf bytes) {
        PacketBuffer buf = new PacketBuffer(bytes);
        buf.writeInt(syncedElements.size());
        try {
            for (Object2ObjectMap.Entry<String, String> entry : syncedElements.object2ObjectEntrySet()) {
                buf.writeStringToBuffer(entry.getKey());
                buf.writeStringToBuffer(entry.getValue());
            }
        } catch (IOException ex) {
            throw new EncoderException(ex);
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncConfig, IMessage> {

        @Override
        public IMessage onMessage(PacketSyncConfig message, MessageContext ctx) {
            ConfigSyncHandler.onSync(message, ctx.netHandler);
            return null;
        }
    }
}
