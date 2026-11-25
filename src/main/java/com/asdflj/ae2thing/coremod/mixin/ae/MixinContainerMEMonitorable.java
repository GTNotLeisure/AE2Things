package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.common.ModernContainerPickup;
import com.asdflj.ae2thing.inventory.IModernContainerPickup;
import com.asdflj.ae2thing.inventory.IPlayerPinInventory;
import com.asdflj.ae2thing.pin.CraftingPinSessions;
import com.asdflj.ae2thing.pin.CraftingPinState;
import com.asdflj.ae2thing.pin.ICraftingPinActions;
import com.asdflj.ae2thing.pin.ICraftingPinState;
import com.asdflj.ae2thing.pin.IPlayerPinActions;
import com.asdflj.ae2thing.pin.PinIdentity;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.ITerminalPins;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.sync.handlers.ObjectSyncHandler;
import appeng.items.contents.PinsHandler;
import appeng.util.item.AEItemStackType;

@SuppressWarnings("UnusedMixin")
@Mixin(ContainerMEMonitorable.class)
public abstract class MixinContainerMEMonitorable extends AEBaseContainer
    implements IModernContainerPickup, IPlayerPinInventory, ICraftingPinState {

    @Shadow(remap = false)
    @Final
    private ITerminalHost host;

    @Unique
    private ObjectSyncHandler<long[]> ae2t$craftingPins;

    @Inject(
        method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lappeng/api/storage/ITerminalHost;Z)V",
        at = @At("RETURN"),
        remap = false)
    private void ae2t$registerCraftingPins(CallbackInfo ci) {
        ae2t$craftingPins = syncRegistrar()
            .objectS2C("ae2thing:craftingPins", CraftingPinState.CODEC, new long[CraftingPinState.WORDS]);
    }

    @Inject(
        method = "detectAndSendChanges",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/container/implementations/ContainerMEMonitorable;updatePins(Z)V",
            remap = false))
    private void ae2t$updateCraftingPins(CallbackInfo ci) {
        if (!AE2ThingConfig.enhancedPins || !(getPlayerInv().player instanceof EntityPlayerMP player)
            || !(host instanceof ITerminalPins terminal)
            || ae2t$craftingPins == null) return;
        IGrid grid = terminal.getGrid();
        if (grid == null) return;
        ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        if (crafting == null) return;
        List<IAEStack<?>> outputs = CraftingPinState.pendingOutputs(crafting.getCpus());
        ICraftingPinActions pins = (ICraftingPinActions) getPinsHandler();
        if (CraftingPinSessions.enter(player, terminal)) pins.ae2t$pruneCraftingPins(outputs);
        long[] bits = pins.ae2t$pendingCraftingPins(outputs);
        if (!Arrays.equals(bits, ae2t$craftingPins.get())) ae2t$craftingPins.set(bits);
    }

    @Override
    public boolean ae2t$isCraftingPinPending(int index) {
        return ae2t$craftingPins != null && CraftingPinState.isPending(ae2t$craftingPins.get(), index);
    }

    @Override
    public boolean ae2t$canCraftTarget() {
        IAEStack<?> target = this.getTargetStack();
        return target != null && CraftingPinState.canCraft(target, monitors.get(target.getStackType()));
    }

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
