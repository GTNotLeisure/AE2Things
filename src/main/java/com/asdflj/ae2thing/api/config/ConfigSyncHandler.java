package com.asdflj.ae2thing.api.config;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;

import com.asdflj.ae2thing.AE2Thing;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ConfigSyncHandler {

    static final Map<String, SyncedConfigElement> syncedElements = new Object2ObjectOpenHashMap<>();
    private static boolean hasSyncedValues;

    static void register(SyncedConfigElement element) {
        syncedElements.put(element.toString(), element);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player
            && !player.playerNetServerHandler.netManager.isLocalChannel()) {
            AE2Thing.proxy.netHandler.sendTo(new PacketSyncConfig(syncedElements.values()), player);
        }
    }

    static void onSync(PacketSyncConfig packet) {
        for (Object2ObjectMap.Entry<String, String> entry : packet.syncedElements.object2ObjectEntrySet()) {
            SyncedConfigElement element = syncedElements.get(entry.getKey());
            if (element == null) continue;
            try {
                element.setSyncValue(entry.getValue());
                hasSyncedValues = true;
            } catch (ConfigException e) {
                ConfigLog.error("Failed to sync config element " + entry.getKey(), e);
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
        if (!hasSyncedValues) return;
        hasSyncedValues = false;
        for (SyncedConfigElement element : syncedElements.values()) element.restoreValue();
    }

    static boolean hasSyncedValues() {
        return hasSyncedValues;
    }
}
