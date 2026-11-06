package com.vcwdfca.appbot.ae;

import net.minecraft.tileentity.TileEntity;

import appeng.api.config.Actionable;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.tile.mana.TilePool;

public final class ManaHelper {

    private ManaHelper() {}

    public static int getCapacity(IManaReceiver receiver) {
        if (receiver instanceof IManaPool pool && pool.isOutputtingPower()) {
            return receiver.getCurrentMana();
        }
        if (receiver instanceof ISparkAttachable attachable) {
            return receiver.getCurrentMana() + Math.max(0, attachable.getAvailableSpaceForMana());
        }
        if (receiver instanceof TilePool pool) {
            return receiver.getCurrentMana() + Math.max(0, pool.getAvailableSpaceForMana());
        }
        if (receiver instanceof IManaPool) {
            return receiver.getCurrentMana() + TilePool.MAX_MANA;
        }
        if (receiver instanceof IManaCollector collector) {
            return collector.getMaxMana();
        }
        if (!receiver.isFull()) {
            return receiver.getCurrentMana() + 1000;
        }
        return 0;
    }

    public static int insert(IManaReceiver receiver, int amount, Actionable mode) {
        if (receiver == null || amount <= 0) {
            return 0;
        }
        final int space = Math.max(0, getCapacity(receiver) - receiver.getCurrentMana());
        final int accepted = Math.min(space, amount);
        if (accepted <= 0) {
            return 0;
        }
        if (mode == Actionable.SIMULATE) {
            return accepted;
        }
        if (receiver.isFull()) {
            return 0;
        }
        final int before = receiver.getCurrentMana();
        receiver.recieveMana(accepted);
        notifyClients(receiver);
        return Math.clamp(receiver.getCurrentMana() - before, 0, accepted);
    }

    public static int extract(IManaReceiver receiver, int amount, Actionable mode) {
        if (receiver == null || amount <= 0) {
            return 0;
        }
        final int before = receiver.getCurrentMana();
        final int requested = Math.min(amount, before);
        if (requested <= 0) {
            return 0;
        }
        if (mode == Actionable.SIMULATE) {
            return requested;
        }
        receiver.recieveMana(-amount);
        notifyClients(receiver);
        return Math.max(requested, before - receiver.getCurrentMana());
    }

    private static void notifyClients(IManaReceiver receiver) {
        if (receiver instanceof TileEntity tile) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile);
        }
    }
}
