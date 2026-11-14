package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.network.CPacketContainerInteraction;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
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

    @Inject(method = "handleVirtualSlotClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$modernInteraction(VirtualMESlot virtualSlot, int mouseButton,
        CallbackInfoReturnable<Boolean> cir) {
        if (!(virtualSlot instanceof VirtualMEMonitorableSlot)) return;

        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        final ItemStack hand = player.inventory.getItemStack();
        final IAEStack<?> hovered = virtualSlot.getAEStack();
        if (ModernContainerInteraction.shouldFillFromNetwork(hovered, hand, mouseButton)) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketContainerInteraction(hovered, GuiScreen.isShiftKeyDown()));
            cir.setReturnValue(true);
            return;
        }

        final MonitorableAction action = ModernContainerInteraction.resolveAction(hovered, hand, mouseButton);
        if (action == null) return;

        this.monitorableContainer.setTargetStack(hovered);
        NetworkHandler.instance.sendToServer(new PacketMonitorableAction(action, -1));
        cir.setReturnValue(true);
    }
}
