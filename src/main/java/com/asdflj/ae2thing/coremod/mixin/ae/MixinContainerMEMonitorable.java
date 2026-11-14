package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.asdflj.ae2thing.common.ModernContainerPickup;
import com.asdflj.ae2thing.inventory.IModernContainerPickup;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(ContainerMEMonitorable.class)
public abstract class MixinContainerMEMonitorable extends AEBaseContainer implements IModernContainerPickup {

    @Shadow(remap = false)
    @Final
    private Map<IAEStackType<?>, IMEMonitor<?>> monitors;

    public MixinContainerMEMonitorable(InventoryPlayer ip, Object anchor) {
        super(ip, anchor);
    }

    @SuppressWarnings({ "rawtypes", "AddedMixinMembersNamePattern" })
    @Override
    public void fillFromNetwork(IAEStack<?> target, boolean moveToPlayer) {
        if (target == null) return;
        final IMEMonitor itemMonitor = this.monitors.get(AEItemStackType.ITEM_STACK_TYPE);
        final IMEMonitor targetMonitor = this.monitors.get(target.getStackType());
        final InventoryPlayer inv = this.getPlayerInv();
        if (itemMonitor == null || targetMonitor == null
            || inv == null
            || !(inv.player instanceof EntityPlayerMP player)) return;
        if (!ModernContainerPickup.fillFromNetwork(
            target,
            moveToPlayer,
            player,
            itemMonitor,
            targetMonitor,
            this.getPowerSource(),
            this.getActionSource())) {
            return;
        }
        this.updateHeld(player);
    }
}
