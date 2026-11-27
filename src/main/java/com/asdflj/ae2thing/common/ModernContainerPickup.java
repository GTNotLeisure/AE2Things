package com.asdflj.ae2thing.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.api.container.EmptyContainerProviders;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

public final class ModernContainerPickup {

    private ModernContainerPickup() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isSupported(@Nullable IAEStack<?> target) {
        if (target == null) return false;
        final IAEStackType type = target.getStackType();
        if (EmptyContainerProviders.allowsNetworkFallback(type)) return true;
        final ItemStack template = EmptyContainerProviders.create(target);
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
        int completed = 0;
        int limit = 1;
        ObjectArrayList<ItemStack> batch = moveToPlayer ? new ObjectArrayList<>() : null;
        while (completed < limit) {
            IAEItemStack container = Platform.poweredExtraction(power, itemMonitor, request, actionSource);
            if (container == null) break;
            ItemStack result = fillOne(container.getItemStack(), target, player, targetMonitor, power, actionSource);
            if (result == null) {
                refund(player, power, itemMonitor, actionSource, container);
                break;
            }
            if (moveToPlayer) {
                if (completed == 0)
                    limit = Math.min(result.getMaxStackSize(), player.inventory.getInventoryStackLimit());
                for (ItemStack stack : batch) {
                    if (!stack.isStackable() || !Platform.isSameItemPrecise(stack, result)) continue;
                    int merge = Math.min(result.stackSize, stack.getMaxStackSize() - stack.stackSize);
                    if (merge <= 0) continue;
                    stack.stackSize += merge;
                    result.stackSize -= merge;
                    if (result.stackSize == 0) break;
                }
                if (result.stackSize > 0) batch.add(result);
            } else player.inventory.setItemStack(result);
            completed++;
        }
        if (completed == 0) return false;
        player.inventory.markDirty();
        if (moveToPlayer) {
            InventoryAdaptor inventory = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
            for (ItemStack stack : batch) {
                ItemStack leftover = inventory.addItems(stack);
                if (leftover != null) player.entityDropItem(leftover, 0);
            }
            player.openContainer.detectAndSendChanges();
        }
        return true;
    }

    @Nullable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ItemStack fillOne(ItemStack container, IAEStack<?> target, EntityPlayerMP player,
        IMEMonitor targetMonitor, IEnergySource power, BaseActionSource actionSource) {
        IAEStackType type = target.getStackType();
        final IAEStack<?> available = Platform
            .poweredExtraction(power, targetMonitor, target.copy(), actionSource, Actionable.SIMULATE);
        ObjectLongPair<ItemStack> filled = available == null || available.getStackSize() <= 0 ? null
            : type.fillContainer(container.copy(), available.copy());

        if (filled == null || filled.left() == null
            || filled.rightLong() <= 0
            || filled.rightLong() > available.getStackSize()) {
            return null;
        }
        available.setStackSize(filled.rightLong());
        final IAEStack<?> extracted = Platform.poweredExtraction(power, targetMonitor, available, actionSource);
        if (extracted == null || extracted.getStackSize() <= 0) {
            return null;
        }
        if (extracted.getStackSize() != filled.rightLong()) {
            filled = type.fillContainer(container.copy(), extracted.copy());
            if (filled.left() == null || filled.rightLong() <= 0 || filled.rightLong() > extracted.getStackSize()) {
                refund(player, power, targetMonitor, actionSource, extracted);
                return null;
            }
            long remainder = extracted.getStackSize() - filled.rightLong();
            if (remainder > 0) refund(
                player,
                power,
                targetMonitor,
                actionSource,
                extracted.copy()
                    .setStackSize(remainder));
        }

        return filled.left();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void refund(EntityPlayerMP player, IEnergySource power, IMEMonitor monitor,
        BaseActionSource actionSource, IAEStack<?> stack) {
        IAEStack<?> leftover = Platform.poweredInsert(power, monitor, stack, actionSource);
        if (leftover == null || leftover.getStackSize() <= 0) return;
        if (leftover instanceof IAEItemStack item) Platform.addToPlayerInvOrDrop(player, item.getItemStack());
        else Platform.handleLeftover(player, leftover);
    }
}
