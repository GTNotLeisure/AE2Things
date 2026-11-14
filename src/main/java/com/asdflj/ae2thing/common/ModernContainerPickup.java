package com.asdflj.ae2thing.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.api.container.EmptyContainerProviders;
import com.asdflj.ae2thing.api.container.IEmptyContainerProvider;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

public final class ModernContainerPickup {

    private ModernContainerPickup() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isSupported(@Nullable IAEStack<?> target) {
        if (target == null) return false;
        final IAEStackType type = target.getStackType();
        final IEmptyContainerProvider provider = EmptyContainerProviders.get(type);
        if (provider == null) return false;
        if (provider.allowNetworkFallback()) return true;
        final ItemStack template = provider.create(target);
        return template != null && type.fillContainer(template.copy(), target.copy())
            .rightLong() > 0;
    }

    @Nullable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ItemStack resolveEmptyContainer(@Nullable IAEStack<?> target, @Nullable IMEMonitor itemMonitor) {
        if (target == null) return null;
        final IAEStackType type = target.getStackType();
        if (!EmptyContainerProviders.isSupported(type)) return null;

        final ItemStack template = EmptyContainerProviders.create(target);
        if (template != null && type.fillContainer(template.copy(), target.copy())
            .rightLong() > 0) {
            return template;
        }

        if (!EmptyContainerProviders.allowsNetworkFallback(type) || itemMonitor == null) return null;
        for (final Object obj : itemMonitor.getStorageList()) {
            if (!(obj instanceof IAEItemStack ais) || ais.getStackSize() <= 0) continue;
            final ItemStack is = ais.getItemStack();
            if (is == null || is.stackSize <= 0) continue;
            if (!type.isContainerItemForType(is)) continue;
            if (type.getStackFromContainerItem(is) != null) continue;
            final ItemStack probe = is.copy();
            probe.stackSize = 1;
            if (type.fillContainer(probe, target.copy())
                .rightLong() <= 0) continue;
            final ItemStack result = is.copy();
            result.stackSize = 1;
            return result;
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "BooleanMethodIsAlwaysInverted" })
    public static boolean fillFromNetwork(@Nullable IAEStack<?> target, boolean moveToPlayer, EntityPlayerMP player,
        @Nullable IMEMonitor itemMonitor, @Nullable IMEMonitor targetMonitor, @Nullable IEnergySource power,
        @Nullable BaseActionSource actionSource) {
        if (player == null || target == null
            || itemMonitor == null
            || targetMonitor == null
            || power == null
            || actionSource == null) return false;
        if (player.inventory.getItemStack() != null) return false;
        final IAEStackType type = target.getStackType();
        if (type == null) return false;

        final ItemStack empty = resolveEmptyContainer(target, itemMonitor);
        if (empty == null) return false;

        final IAEItemStack request = AEItemStack.create(empty.copy());
        request.setStackSize(1);
        final IAEItemStack container = Platform.poweredExtraction(power, itemMonitor, request, actionSource);
        if (container == null) return false;

        final IAEStack<?> available = Platform
            .poweredExtraction(power, targetMonitor, target.copy(), actionSource, Actionable.SIMULATE);
        final ObjectLongPair<ItemStack> filled = available == null || available.getStackSize() <= 0 ? null
            : type.fillContainer(
                container.getItemStack()
                    .copy(),
                available);

        if (filled == null || filled.left() == null || filled.rightLong() <= 0) {
            refund(power, itemMonitor, actionSource, container);
            return false;
        }
        available.setStackSize(filled.rightLong());
        if (Platform.poweredExtraction(power, targetMonitor, available, actionSource) == null) {
            refund(power, itemMonitor, actionSource, container);
            return false;
        }

        if (moveToPlayer) {
            Platform.addToPlayerInvOrDrop(player, filled.left());
        } else {
            player.inventory.setItemStack(filled.left());
        }
        player.inventory.markDirty();
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void refund(IEnergySource power, IMEMonitor itemMonitor, BaseActionSource actionSource,
        IAEItemStack container) {
        container.setStackSize(1);
        Platform.poweredInsert(power, itemMonitor, container, actionSource);
    }
}
