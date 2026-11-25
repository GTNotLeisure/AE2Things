package com.asdflj.ae2thing.api.config;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

import com.asdflj.ae2thing.AE2Thing;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ConfigSyncHandler {

    static final Object2ObjectMap<String, SyncedConfigElement> syncedElements = new Object2ObjectOpenHashMap<>();
    private static boolean hasSyncedValues;
    private static INetHandler syncHandler;

    static void register(SyncedConfigElement element) {
        if (syncedElements.putIfAbsent(element.getId(), element) != null)
            throw new IllegalArgumentException("Already registered sync element: " + element.getId());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player
            && !player.playerNetServerHandler.netManager.isLocalChannel()) {
            AE2Thing.proxy.netHandler.sendTo(new PacketSyncConfig(syncedElements.values()), player);
        }
    }

    static void onSync(PacketSyncConfig packet, INetHandler handler) {
        if (hasSyncedValues && syncHandler != handler) restoreSyncedValues();
        syncHandler = handler;
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
        AE2Thing.proxy.scheduleClientTask(() -> restoreSyncedValues(event.handler));
    }

    static void restoreSyncedValues(INetHandler handler) {
        if (syncHandler == handler) restoreSyncedValues();
    }

    private static void restoreSyncedValues() {
        if (!hasSyncedValues) return;
        hasSyncedValues = false;
        for (SyncedConfigElement element : syncedElements.values()) {
            try {
                element.restoreValue();
            } catch (RuntimeException ex) {
                ConfigLog.error("Failed to restore config element " + element.getId(), ex);
            }
            hasSyncedValues |= element.isSynced();
        }
        if (!hasSyncedValues) syncHandler = null;
    }

    static boolean hasSyncedValues() {
        return hasSyncedValues;
    }
}
