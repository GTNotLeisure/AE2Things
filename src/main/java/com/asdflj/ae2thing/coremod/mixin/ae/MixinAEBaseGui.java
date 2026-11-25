package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.TerminalMenu;
import com.asdflj.ae2thing.client.ModernContainerInteraction;
import com.asdflj.ae2thing.client.event.AEGuiCloseEvent;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.pin.PinHand;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.network.CPacketPlayerPinAction;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMESlot;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import codechicken.nei.recipe.StackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiContainer {

    public MixinAEBaseGui(Container container) {
        super(container);
    }

    @Shadow(remap = false)
    protected abstract GuiScrollbar getScrollBar();

    @Inject(method = "handleItemTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$hideEntryTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip,
        CallbackInfoReturnable<List<String>> cir) {
        if (!ModernContainerInteraction.hidesEntryTooltip()) return;
        currentToolTip.clear();
        cir.setReturnValue(currentToolTip);
    }

    @Inject(method = "handleMouseClick", at = @At(value = "HEAD"), cancellable = true)
    protected void handleMouseClick(Slot slot, int slotIdx, int clickedButton, int clickType, CallbackInfo ci) {
        // noinspection ConstantValue
        if (AE2ThingConfig.enhancedPins && AE2ThingConfig.modernInteraction
            && (Object) this instanceof GuiMEMonitorable gui
            && gui.hasPinHost
            && clickedButton == 0
            && (clickType == 0 || clickType == 1)
            && GuiScreen.isCtrlKeyDown()
            && !Keyboard.isKeyDown(Keyboard.KEY_SPACE)
            && PinHand.read()
                .isEmpty()
            && (slot instanceof SlotPlayerInv || slot instanceof SlotPlayerHotBar)
            && slot.inventory == this.mc.thePlayer.inventory
            && slot.getHasStack()) {
            AE2Thing.proxy.netHandler.sendToServer(
                new CPacketPlayerPinAction(
                    this.inventorySlots.windowId,
                    GuiScreen.isShiftKeyDown() ? CPacketPlayerPinAction.STORE_INVENTORY
                        : CPacketPlayerPinAction.TOGGLE_INVENTORY,
                    slot.slotNumber,
                    null));
            ci.cancel();
            return;
        }
        if (clickedButton == 1 && clickType == 0
            && (slot instanceof SlotPlayerInv || slot instanceof SlotPlayerHotBar)
            && slot.getHasStack()) {
            ItemStack item = slot.getStack();
            TerminalMenu menu = new TerminalMenu();
            for (int i = 0; i < menu.getItems()
                .size(); i++) {
                ItemStack term = menu.getItems()
                    .get(i);
                if (StackInfo.equalItemAndNBT(term, item, true)) {
                    menu.openTerminal(i);
                    ci.cancel();
                    break;
                }
            }

        }
    }

    @Inject(method = "onGuiClosed", at = @At(value = "HEAD"))
    public void onGuiClosed(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new AEGuiCloseEvent((AEBaseGui) (Object) this));
    }

    @Inject(method = "handleMouseInput", at = @At("HEAD"))
    public void handleMouseInput(CallbackInfo ci) {
        if (this.getScrollBar() != null) {
            if (!Mouse.isButtonDown(0)) {
                ((AccessorGuiScrollbar) this.getScrollBar()).setIsLatestClickOnScrollbar(false);
            }
        }
    }

    @Redirect(
        method = "drawVirtualSlots",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/gui/slots/VirtualMESlot;drawStackAndOverlay(Lnet/minecraft/client/Minecraft;II)Z"),
        remap = false)
    private boolean ae2thing$deferDualTerminalVirtualSlots(VirtualMESlot slot, Minecraft mc, int mouseX, int mouseY) {
        if ((Object) this instanceof GuiWirelessDualInterfaceTerminal) return false;
        return slot.drawStackAndOverlay(mc, mouseX, mouseY);
    }
}
