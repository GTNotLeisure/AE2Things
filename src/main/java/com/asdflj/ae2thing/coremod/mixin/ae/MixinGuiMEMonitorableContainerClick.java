package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.client.pin.PinHand;
import com.asdflj.ae2thing.client.pin.ScrollingPlayerPinSlot;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.network.CPacketContainerInteraction;
import com.asdflj.ae2thing.network.CPacketPlayerPinAction;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.client.gui.slots.VirtualMEPinSlot;
import appeng.client.gui.slots.VirtualMESlot;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMonitorableAction;
import appeng.helpers.MonitorableAction;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiMEMonitorable.class)
public abstract class MixinGuiMEMonitorableContainerClick {

    @Final
    @Shadow(remap = false)
    private ContainerMEMonitorable monitorableContainer;
    @Final
    @Shadow(remap = false)
    public boolean hasPinHost;

    @Shadow(remap = false)
    private boolean handleMonitorableSlotClick(VirtualMESlot virtualSlot, int mouseButton) {
        throw new AssertionError();
    }

    @Unique
    private boolean ae2t$nativeRoute;
    @Unique
    private boolean ae2t$dragging;

    @Redirect(
        method = "handleDragVirtualSlot",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/gui/implementations/GuiMEMonitorable;handleMonitorableSlotClick(Lappeng/client/gui/slots/VirtualMESlot;I)Z"),
        remap = false)
    private boolean ae2t$dragClick(GuiMEMonitorable gui, VirtualMESlot virtualSlot, int mouseButton) {
        ae2t$dragging = true;
        try {
            return handleMonitorableSlotClick(virtualSlot, mouseButton);
        } finally {
            ae2t$dragging = false;
        }
    }

    @Inject(method = "handleMonitorableSlotClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$modernInteraction(VirtualMESlot virtualSlot, int mouseButton,
        CallbackInfoReturnable<Boolean> cir) {
        if (ae2t$nativeRoute || !(virtualSlot instanceof VirtualMEMonitorableSlot)) return;
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        boolean enhanced = AE2ThingConfig.enhancedPins && hasPinHost;
        VirtualMEPinSlot proxy = virtualSlot instanceof ScrollingPlayerPinSlot scrolling ? scrolling.asPinSlot() : null;
        if (enhanced && virtualSlot instanceof ScrollingPlayerPinSlot scrolling && scrolling.isPadding()) {
            cir.setReturnValue(true);
            return;
        }
        VirtualMEPinSlot pin = virtualSlot instanceof VirtualMEPinSlot fixed ? fixed : proxy;
        if (enhanced && AE2ThingConfig.modernInteraction
            && Keyboard.isKeyDown(Keyboard.KEY_SPACE)
            && PinHand.read()
                .ghost() != null) {
            cir.setReturnValue(true);
            return;
        }
        if (enhanced && AE2ThingConfig.modernInteraction && !Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            PinHand hand = PinHand.read();
            IAEStack<?> hovered = virtualSlot.getAEStack();
            boolean shift = GuiScreen.isShiftKeyDown();
            boolean leftOrRight = mouseButton == 0 || mouseButton == 1;
            boolean playerPin = pin != null && !pin.isCraftingSlot();
            if (!ae2t$dragging && playerPin && hovered == null && hand.effective() != null && leftOrRight) {
                IAEStack<?> key = hand.pinKey(mouseButton);
                if (key != null) ae2t$pinAction(CPacketPlayerPinAction.SET_EMPTY_SLOT, pin.getSlotIndex(), key);
                cir.setReturnValue(true);
                return;
            }
            if (hand.ghost() != null) {
                cir.setReturnValue(true);
                return;
            }
            if (!ae2t$dragging && mouseButton == 0 && GuiScreen.isCtrlKeyDown() && !shift && hand.isEmpty()) {
                if (hovered != null) ae2t$pinAction(CPacketPlayerPinAction.TOGGLE_STACK, -1, hovered);
                cir.setReturnValue(true);
                return;
            }
            if (!ae2t$dragging && mouseButton == 1 && shift && hand.isEmpty() && pin != null) {
                if (playerPin) ae2t$pinAction(CPacketPlayerPinAction.REMOVE_SLOT, pin.getSlotIndex(), null);
                else ae2t$send(MonitorableAction.UNSET_PIN, null, pin.getSlotIndex());
                cir.setReturnValue(true);
                return;
            }
            if (ae2t$containerAction(hovered, hand.real(), mouseButton)) {
                cir.setReturnValue(true);
                return;
            }
            if (leftOrRight) {
                IAEItemStack item = hovered instanceof IAEItemStack ais ? ais : null;
                if (mouseButton == 0) {
                    if (shift) ae2t$send(MonitorableAction.SHIFT_CLICK, item, -1);
                    else if (hovered != null && hovered.getStackSize() == 0 && hand.real() == null)
                        ae2t$send(MonitorableAction.AUTO_CRAFT, hovered, -1);
                    else ae2t$send(MonitorableAction.PICKUP_OR_SET_DOWN, item, -1);
                } else ae2t$send(
                    shift ? MonitorableAction.PICKUP_SINGLE : MonitorableAction.SPLIT_OR_PLACE_SINGLE,
                    item,
                    -1);
                cir.setReturnValue(true);
                return;
            }
        } else if (!ae2t$dragging
            && ae2t$containerAction(virtualSlot.getAEStack(), player.inventory.getItemStack(), mouseButton)) {
                cir.setReturnValue(true);
                return;
            }
        if (enhanced && proxy != null) {
            ae2t$nativeRoute = true;
            try {
                cir.setReturnValue(handleMonitorableSlotClick(proxy, mouseButton));
            } finally {
                ae2t$nativeRoute = false;
            }
        }
    }

    @Unique
    private boolean ae2t$containerAction(IAEStack<?> hovered, ItemStack hand, int button) {
        if (ModernContainerInteraction.shouldFillFromNetwork(hovered, hand, button)) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketContainerInteraction(hovered, GuiScreen.isShiftKeyDown()));
            return true;
        }
        MonitorableAction action = ModernContainerInteraction.resolveAction(hovered, hand, button);
        if (action == null) return false;
        ae2t$send(action, hovered, -1);
        return true;
    }

    @Unique
    private void ae2t$pinAction(int operation, int index, IAEStack<?> key) {
        AE2Thing.proxy.netHandler
            .sendToServer(new CPacketPlayerPinAction(monitorableContainer.windowId, operation, index, key));
    }

    @Unique
    private void ae2t$send(MonitorableAction action, IAEStack<?> key, int index) {
        monitorableContainer.setTargetStack(key);
        NetworkHandler.instance.sendToServer(new PacketMonitorableAction(action, index));
    }
}
