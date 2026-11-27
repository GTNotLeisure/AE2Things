package com.asdflj.ae2thing.coremod.mixin.ae;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.config.ConfigurationManager;
import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketPlayerPinAction;
import com.asdflj.ae2thing.pin.CraftingPinMode;
import com.asdflj.ae2thing.client.pin.CraftingPinRenderer;
import com.asdflj.ae2thing.client.pin.IPlayerPinRepo;
import com.asdflj.ae2thing.client.pin.PinHand;
import com.asdflj.ae2thing.client.pin.PlayerPinLayout;
import com.asdflj.ae2thing.client.pin.ScrollingPlayerPinSlot;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.pin.ICraftingPinState;

import appeng.api.config.PinsRows;
import appeng.api.storage.data.IAEStackType;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.client.gui.slots.VirtualMEPinSlot;
import appeng.client.gui.slots.VirtualMESlot;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.me.ItemRepo;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketPinsUpdate;
import appeng.items.contents.PinList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiMEMonitorable.class)
public abstract class MixinGuiMEMonitorablePins extends AEBaseGui {

    @Shadow(remap = false)
    @Final
    private ItemRepo repo;

    @Shadow(remap = false)
    @Final
    public boolean hasPinHost;

    @Shadow(remap = false)
    protected int rows;

    @Shadow(remap = false)
    private int perRow;

    @Shadow(remap = false)
    protected int offsetRepoX;

    @Shadow(remap = false)
    protected int offsetRepoY;

    @Shadow(remap = false)
    private PinsRows craftingPinsRows;

    @Shadow(remap = false)
    private PinsRows playerPinsRows;

    @Shadow(remap = false)
    protected VirtualMEPinSlot[] pinSlots;

    @Shadow(remap = false)
    protected VirtualMEMonitorableSlot[] monitorableSlots;

    @Shadow(remap = false)
    private GuiImgButton pinsStateButton;

    @Shadow(remap = false)
    private int scrollBarRepoSize;

    @Shadow(remap = false)
    private boolean checkTypeFilter(IAEStackType<?> type) {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private void setScrollBar() {
        throw new AssertionError();
    }

    protected MixinGuiMEMonitorablePins(Container container) {
        super(container);
    }

    @Unique
    private boolean ae2t$enabled() {
        return AE2ThingConfig.enhancedPins && hasPinHost;
    }

    @Unique
    private PlayerPinLayout ae2t$layout() {
        return new PlayerPinLayout(
            rows,
            perRow,
            Math.min(craftingPinsRows.ordinal(), AEConfig.instance.maxCraftingPinRows),
            Math.min(playerPinsRows.ordinal(), AEConfig.instance.maxPlayerPinRows),
            AE2ThingConfig.scrollPlayerPins);
    }

    @Inject(method = "adjustPinsSize", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$keepSavedRows(CallbackInfo ci) {
        if (ae2t$enabled()) ci.cancel();
    }

    @Redirect(
        method = "initGui",
        at = @At(value = "INVOKE", target = "Lappeng/client/me/ItemRepo;setVisiblePinRows(II)V", remap = false))
    private void ae2t$visibleRows(ItemRepo itemRepo, int craftingRows, int playerRows) {
        if (!ae2t$enabled()) {
            itemRepo.setVisiblePinRows(craftingRows, playerRows);
            return;
        }
        PlayerPinLayout layout = ae2t$layout();
        ((IPlayerPinRepo) itemRepo).ae2t$setPinLayout(layout);
        itemRepo
            .setVisiblePinRows(layout.craftingRows(), layout.scrolling() ? layout.capacity() / 9 : layout.playerRows());
    }

    @Inject(method = "initGui", at = @At("TAIL"))
    private void ae2t$rebuildSlots(CallbackInfo ci) {
        if (!ae2t$enabled()) {
            ((IPlayerPinRepo) repo).ae2t$setPinLayout(null);
            return;
        }
        PlayerPinLayout layout = ae2t$layout();
        List<VirtualMESlot> registered = ((AccessorAEBaseGuiPins) this).ae2t$getVirtualSlots();
        if (pinSlots != null) registered.removeAll(Arrays.asList(pinSlots));
        if (monitorableSlots != null) registered.removeAll(Arrays.asList(monitorableSlots));
        List<VirtualMEPinSlot> fixed = new ObjectArrayList<>();
        ae2t$section(fixed, layout.craftingRows(), 0, true);
        ae2t$section(fixed, layout.playerRows(), layout.craftingRows(), false);
        pinSlots = fixed.toArray(new VirtualMEPinSlot[0]);
        int fixedRows = layout.craftingRows() + layout.playerRows();
        monitorableSlots = new VirtualMEMonitorableSlot[layout.listRows() * layout.width()];
        for (int y = 0; y < layout.listRows(); y++) {
            for (int x = 0; x < layout.width(); x++) {
                int index = y * layout.width() + x;
                int sx = offsetRepoX + x * 18;
                int sy = offsetRepoY + (fixedRows + y) * 18;
                VirtualMEMonitorableSlot slot = layout.scrolling()
                    ? new ScrollingPlayerPinSlot(sx, sy, repo, index, this::checkTypeFilter)
                    : new VirtualMEMonitorableSlot(sx, sy, repo, index, this::checkTypeFilter);
                monitorableSlots[index] = slot;
                registerVirtualSlots(slot);
            }
        }
        ((IPlayerPinRepo) repo).ae2t$setPinLayout(layout);
        repo.setVisiblePinRows(layout.craftingRows(), layout.scrolling() ? layout.capacity() / 9 : layout.playerRows());
        setScrollBar();
    }

    @Unique
    private void ae2t$section(List<VirtualMEPinSlot> result, int count, int offset, boolean crafting) {
        for (int y = 0; y < count; y++) {
            for (int x = 0; x < 9; x++) {
                VirtualMEPinSlot slot = new VirtualMEPinSlot(
                    offsetRepoX + x * 18,
                    offsetRepoY + (offset + y) * 18,
                    repo,
                    (crafting ? 0 : PinList.PLAYER_OFFSET) + y * 9 + x,
                    this::checkTypeFilter,
                    crafting);
                result.add(slot);
                registerVirtualSlots(slot);
            }
        }
    }

    @Inject(method = "setScrollBar", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$scrollRange(CallbackInfo ci) {
        if (!ae2t$enabled()) return;
        PlayerPinLayout layout = ae2t$layout();
        scrollBarRepoSize = repo.size();
        getScrollBar().setTop(offsetRepoY + (layout.craftingRows() + layout.playerRows()) * 18)
            .setLeft(166 + offsetRepoX)
            .setHeight(layout.listRows() * 18 - 2);
        getScrollBar().setRange(0, layout.scrollMax(repo.size()), Math.max(1, layout.listRows() / 6));
        ci.cancel();
    }

    @Inject(method = "mouseWheelEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$guardPinWheel(int x, int y, int wheel, CallbackInfoReturnable<Boolean> cir) {
        if (!ae2t$enabled() || !isShiftKeyDown()) return;
        VirtualMESlot slot = getVirtualMESlotUnderMouse();
        if (slot instanceof ScrollingPlayerPinSlot scrolling && scrolling.isPadding()
            || slot instanceof VirtualMEMonitorableSlot && AE2ThingConfig.modernInteraction
                && PinHand.read()
                    .ghost() != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void ae2t$pinButton(GuiButton btn, CallbackInfo ci) {
        if (!ae2t$enabled() || btn != pinsStateButton) return;
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            if (Mouse.isButtonDown(1)) {
                AE2Thing.proxy.netHandler.sendToServer(new CPacketPlayerPinAction(inventorySlots.windowId,
                    CPacketPlayerPinAction.CYCLE_CRAFTING_MODE, -1, null));
                ci.cancel();
                return;
            }
            ConfigurationManager
                .setClientBoolean(AE2ThingConfig.class, "scrollPlayerPins", !AE2ThingConfig.scrollPlayerPins);
            reinitalize();
            getScrollBar().setCurrentScroll(0);
            ci.cancel();
            return;
        }
        int crafting = craftingPinsRows.ordinal();
        int player = playerPinsRows.ordinal();
        int delta = Mouse.isButtonDown(1) ? -1 : 1;
        if (isCtrlKeyDown()) {
            if (((ICraftingPinState) inventorySlots).ae2t$craftingMode() == CraftingPinMode.AUTO) {
                ci.cancel();
                return;
            }
            if (delta < 0 || crafting < AEConfig.instance.maxCraftingPinRows) crafting = Math.max(0, crafting + delta);
        } else if (delta < 0 || player < AEConfig.instance.maxPlayerPinRows) {
            player = Math.max(0, player + delta);
        }
        try {
            NetworkHandler.instance
                .sendToServer(new PacketPinsUpdate(PinsRows.fromOrdinal(crafting), PinsRows.fromOrdinal(player)));
        } catch (IOException e) {
            AELog.debug(e);
        }
        ci.cancel();
    }

    @Redirect(
        method = "drawFG",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/gui/slots/VirtualMEPinSlot;drawSlotsBackground([Lappeng/client/gui/slots/VirtualMEPinSlot;Lnet/minecraft/client/Minecraft;F)V"),
        remap = false)
    private void ae2t$pinBackground(VirtualMEPinSlot[] slots, Minecraft mc, float z) {
        if (!ae2t$enabled() || !AE2ThingConfig.scrollPlayerPins) {
            VirtualMEPinSlot.drawSlotsBackground(slots, mc, z);
            return;
        }
        List<VirtualMEPinSlot> visible = new ObjectArrayList<>(slots);
        for (VirtualMEMonitorableSlot slot : monitorableSlots) {
            if (slot instanceof ScrollingPlayerPinSlot scrolling) {
                VirtualMEPinSlot pin = scrolling.asPinSlot();
                if (pin != null) visible.add(pin);
            }
        }
        VirtualMEPinSlot.drawSlotsBackground(visible.toArray(new VirtualMEPinSlot[0]), mc, z);
    }

    @Inject(method = "drawFG", at = @At("TAIL"), remap = false)
    private void ae2t$craftingBorders(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
        if (ae2t$enabled() && inventorySlots instanceof ICraftingPinState state)
            CraftingPinRenderer.draw(pinSlots, state);
    }
}
