package com.asdflj.ae2thing.network;

import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.client.me.InfinityCellContentsCache;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketCellContents implements IMessage {

    private String uuid;
    private String typeId;
    private int offset;
    private NBTTagCompound payload;

    @SuppressWarnings("unused")
    public SPacketCellContents() {}

    public SPacketCellContents(String uuid, String typeId, int offset, NBTTagCompound payload) {
        this.uuid = uuid;
        this.typeId = typeId;
        this.offset = offset;
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = ByteBufUtils.readUTF8String(buf);
        this.typeId = ByteBufUtils.readUTF8String(buf);
        this.offset = buf.readInt();
        this.payload = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uuid);
        ByteBufUtils.writeUTF8String(buf, this.typeId);
        buf.writeInt(this.offset);
        ByteBufUtils.writeTag(buf, this.payload);
    }

    public static class Handler implements IMessageHandler<SPacketCellContents, IMessage> {

        @Override
        public IMessage onMessage(SPacketCellContents message, MessageContext ctx) {
            if (message.payload != null) {
                InfinityCellContentsCache.receive(message.uuid, message.typeId, message.offset, message.payload);
            }
            return null;
        }
    }
}
