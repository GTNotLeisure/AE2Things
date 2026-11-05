package com.asdflj.ae2thing.network;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.util.ItemGuiHintCache;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketItemGuiHint implements IMessage {

    private int x;
    private ItemStack stack;

    @SuppressWarnings("unused")
    public SPacketItemGuiHint() {}

    public SPacketItemGuiHint(int x, ItemStack stack) {
        this.x = x;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<SPacketItemGuiHint, IMessage> {

        @Override
        public IMessage onMessage(SPacketItemGuiHint message, MessageContext ctx) {
            ItemGuiHintCache.put(message.x, message.stack);
            return null;
        }
    }
}
