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
    private int limit;
    private long rev;

    @SuppressWarnings("unused")
    public CPacketCellContentsRequest() {}

    public CPacketCellContentsRequest(String uuid, String typeId, int offset, int limit, long rev) {
        this.uuid = uuid;
        this.typeId = typeId;
        this.offset = offset;
        this.limit = limit;
        this.rev = rev;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = ByteBufUtils.readUTF8String(buf);
        this.typeId = ByteBufUtils.readUTF8String(buf);
        this.offset = buf.readInt();
        this.limit = buf.readInt();
        this.rev = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uuid);
        ByteBufUtils.writeUTF8String(buf, this.typeId);
        buf.writeInt(this.offset);
        buf.writeInt(this.limit);
        buf.writeLong(this.rev);
    }

    public static class Handler implements IMessageHandler<CPacketCellContentsRequest, IMessage> {

        private static final int MAX_REQUESTS_PER_SECOND = 64;
        private static final Reference2ObjectMap<EntityPlayerMP, Limit> LIMITS = new Reference2ObjectOpenHashMap<>();

        public static void forget(EntityPlayerMP player) {
            LIMITS.remove(player);
        }

        @Override
        public IMessage onMessage(CPacketCellContentsRequest message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || !acquire(player)) return null;
            final IAEStackType<?> type = AEStackTypeRegistry.getType(message.typeId);
            if (type == null) return null;
            final StorageManager manager = AE2ThingAPI.instance()
                .getStorageManager();
            if (manager == null) return null;

            int offset = Math.clamp(message.offset, 0, InfinityStorageCellSnapshot.MAX_OFFSET);
            final int limit = Math.clamp(message.limit, 0, InfinityStorageCellSnapshot.PAGE_SIZE);
            NBTTagCompound payload = InfinityStorageCellSnapshot.emptyPage(limit);
            final DataStorage storage = manager.findStorage(message.uuid, type);
            if (storage != null) {
                final long currentRev = storage.revision();
                if (limit == 0) {
                    payload = InfinityStorageCellSnapshot.stats(storage);
                } else {
                    if (offset > 0 && message.rev != currentRev) {
                        offset = 0;
                    }

                    final boolean forceContents = message.rev < 0;

                    if (forceContents || offset > 0 || message.rev != currentRev) {
                        payload = InfinityStorageCellSnapshot.page(storage, offset, limit);
                    } else {
                        payload = InfinityStorageCellSnapshot.stats(storage);
                    }
                }
            } else {
                offset = 0;
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
