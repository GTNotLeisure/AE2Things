package com.asdflj.ae2thing.pin;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalPins;
import appeng.items.contents.PinsHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public final class CraftingPinSessions {

    private static final Object2ObjectMap<UUID, Reference2ObjectMap<Object, ITerminalPins>> HOSTS = new Object2ObjectOpenHashMap<>();

    public static boolean enter(EntityPlayerMP player, ITerminalPins host) {
        Object key = host instanceof IGuiItemObject item ? item.getItemStack() : host;
        return enter(player.getUniqueID(), key, host);
    }

    static boolean enter(UUID player, Object key, ITerminalPins host) {
        return HOSTS.computeIfAbsent(player, _ -> new Reference2ObjectOpenHashMap<>())
            .put(key, host) == null;
    }

    static Reference2ObjectMap<Object, ITerminalPins> take(UUID player) {
        return HOSTS.remove(player);
    }

    public static void clear() {
        HOSTS.clear();
    }

    private static void finish(EntityPlayerMP player) {
        Reference2ObjectMap<Object, ITerminalPins> hosts = take(player.getUniqueID());
        if (hosts == null || !AE2ThingConfig.enhancedPins) return;
        for (ITerminalPins host : hosts.values()) {
            IGrid grid = host.getGrid();
            if (grid == null) continue;
            ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
            if (crafting == null) continue;
            PinsHandler handler = host.getPinsHandler(player);
            if (handler != null) {
                ((ICraftingPinActions) handler)
                    .ae2t$pruneCraftingPins(CraftingPinState.pendingOutputs(crafting.getCpus()));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP player
            && player.openContainer == player.inventoryContainer) finish(player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP player) finish(player);
    }
}
