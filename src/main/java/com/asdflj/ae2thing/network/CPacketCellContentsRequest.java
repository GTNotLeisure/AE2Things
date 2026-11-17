package com.asdflj.ae2thing.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.DataStorage;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;

import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStackType;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class CPacketCellContentsRequest implements IMessage {

    private String uuid;
    private String typeId;
    private int offset;

    @SuppressWarnings("unused")
    public CPacketCellContentsRequest() {}

    public CPacketCellContentsRequest(String uuid, String typeId, int offset) {
        this.uuid = uuid;
        this.typeId = typeId;
        this.offset = offset;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = ByteBufUtils.readUTF8String(buf);
        this.typeId = ByteBufUtils.readUTF8String(buf);
        this.offset = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uuid);
        ByteBufUtils.writeUTF8String(buf, this.typeId);
        buf.writeInt(this.offset);
    }

    public static class Handler implements IMessageHandler<CPacketCellContentsRequest, IMessage> {

        private static final int MAX_REQUESTS_PER_SECOND = 32;
        private static final Reference2ObjectMap<EntityPlayerMP, Limit> LIMITS = new Reference2ObjectOpenHashMap<>();

        public static void forget(EntityPlayerMP player) {
            LIMITS.remove(player);
        }

        @Override
        public IMessage onMessage(CPacketCellContentsRequest message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || !Handler.acquire(player)) return null;
            final IAEStackType<?> type = AEStackTypeRegistry.getType(message.typeId);
            if (type == null) return null;
            final StorageManager manager = AE2ThingAPI.instance()
                .getStorageManager();
            if (manager == null) return null;
            final int offset = Math.clamp(message.offset, 0, InfinityStorageCellSnapshot.MAX_OFFSET);
            NBTTagCompound payload = InfinityStorageCellSnapshot.emptyPage();
            if (message.uuid != null && manager.hasContent(message.uuid)) {
                final DataStorage storage = manager.getStorage(message.uuid, type);
                if (storage != null) payload = InfinityStorageCellSnapshot.page(storage, offset);
            }
            AE2Thing.proxy.netHandler
                .sendTo(new SPacketCellContents(message.uuid, message.typeId, offset, payload), player);
            return null;
        }

        private static boolean acquire(EntityPlayerMP player) {
            final long now = System.currentTimeMillis();
            Limit limit = LIMITS.get(player);
            if (limit == null) {
                limit = new Limit();
                LIMITS.put(player, limit);
            }
            if (now - limit.window >= 1000L) {
                limit.window = now;
                limit.count = 0;
            }
            if (limit.count >= MAX_REQUESTS_PER_SECOND) return false;
            limit.count++;
            return true;
        }

        private static final class Limit {

            private long window;
            private int count;
        }
    }
}
