package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.asdflj.ae2thing.common.ModernContainerPickup;
import com.asdflj.ae2thing.inventory.IModernContainerPickup;
import com.asdflj.ae2thing.inventory.IPlayerPinInventory;
import com.asdflj.ae2thing.pin.IPlayerPinActions;
import com.asdflj.ae2thing.pin.PinIdentity;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.items.contents.PinsHandler;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(ContainerMEMonitorable.class)
public abstract class MixinContainerMEMonitorable extends AEBaseContainer
    implements IModernContainerPickup, IPlayerPinInventory {

    @Shadow(remap = false)
    protected abstract ItemStack shiftStoreItem(ItemStack input);

    @Shadow(remap = false)
    public abstract PinsHandler getPinsHandler();

    @Unique
    private AppEngSlot ae2t$playerSlot(EntityPlayerMP player, int index) {
        if (index < 0 || index >= this.inventorySlots.size()) return null;
        Slot slot = this.inventorySlots.get(index);
        if (!(slot instanceof SlotPlayerInv || slot instanceof SlotPlayerHotBar)) return null;
        AppEngSlot aeSlot = (AppEngSlot) slot;
        if (slot.inventory != player.inventory || !aeSlot.isEnabled()
            || !slot.canTakeStack(player)
            || !this.isValidSrcSlotForTransfer(aeSlot)) return null;
        return aeSlot;
    }

    @Override
    public IAEStack<?> ae2t$inventoryPinKey(EntityPlayerMP player, int index) {
        AppEngSlot slot = ae2t$playerSlot(player, index);
        return slot == null ? null : PinIdentity.fromInventoryItem(slot.getStack());
    }

    @Override
    public void ae2t$storeAndPin(EntityPlayerMP player, int index) {
        AppEngSlot slot = ae2t$playerSlot(player, index);
        if (slot == null || !slot.getHasStack()) return;
        ItemStack before = slot.getStack()
            .copy();
        IAEStack<?> key = PinIdentity.fromInventoryItem(before);
        if (key == null || key.getStackSize() <= 0) return;
        ItemStack remaining = this.shiftStoreItem(before.copy());
        slot.putStack(remaining);
        this.detectAndSendChanges();
        if (PinIdentity.wasInserted(key, remaining)) {
            ((IPlayerPinActions) this.getPinsHandler()).ae2t$autoAdd(key);
        }
    }

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
