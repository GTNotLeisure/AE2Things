package com.asdflj.ae2thing.coremod.mixin.ae;

import static com.asdflj.ae2thing.api.Constants.MessageType.ADD_PINNED_ITEM;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;

import appeng.api.config.PinsRows;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.items.contents.PinsHandler;

@SuppressWarnings("UnusedMixin")
@Mixin(ContainerCraftConfirm.class)
public abstract class MixinContainerCraftConfirm extends AEBaseContainer {

    @Shadow(remap = false)
    protected ICraftingJob<?> result;

    @Shadow(remap = false)
    public abstract boolean isSimulation();

    @Shadow(remap = false)
    public abstract IGrid getGrid();

    @Unique
    private IAEStack<?> ae2t$is = null;

    public MixinContainerCraftConfirm(InventoryPlayer ip, ITerminalHost anchor) {
        super(ip, anchor);
    }

    @Inject(method = "setItemToCraft", at = @At("HEAD"), remap = false)
    public void setItemToCraft(IAEStack<?> itemToCraft, CallbackInfo ci) {
        if (itemToCraft != null) {
            ae2t$is = itemToCraft.copy();
        }
    }

    @Redirect(
        method = "pushCraftedItemToPins",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/items/contents/PinsHandler;getCraftingPinsRows()Lappeng/api/config/PinsRows;"),
        remap = false)
    private PinsRows ae2t$allowAutomaticCraftingRow(PinsHandler pins) {
        return AE2ThingConfig.enhancedPins ? PinsRows.ONE : pins.getCraftingPinsRows();
    }

    @Inject(
        method = "startJob(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/container/implementations/ContainerCraftConfirm;switchToOriginalGUI()V"),
        remap = false)
    public void startJob(boolean followCraft, CallbackInfo ci) {
        if (this.result != null && !this.isSimulation() && getGrid() != null && ae2t$is != null) {
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate(ADD_PINNED_ITEM);
            piu.appendStack(ae2t$is);
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) this.getPlayerInv().player);
            ae2t$is = null;
        }
    }
}
