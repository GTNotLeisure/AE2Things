package com.vcwdfca.appbot.part;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.vcwdfca.appbot.ae.AEManaStack;
import com.vcwdfca.appbot.ae.ManaExternalInventory;

import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartManaExportBus extends SharedManaBus {

    private final BaseActionSource source;

    public PartManaExportBus(ItemStack is) {
        super(is);
        this.getConfigManager()
            .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.source = new MachineSource(this);
    }

    @Override
    public IIcon getFaceIcon() {
        return FCPartsTexture.PartFluidExportBus.getIcon();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 40, this.isSleeping(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        final ManaExternalInventory external = ManaExternalInventory.of(this.getConnectedTE());
        if (external == null) {
            return TickRateModulation.SLOWER;
        }
        try {
            final IMEMonitor<AEManaStack> network = this.manaInventory();
            if (network == null) {
                return TickRateModulation.SLOWER;
            }
            final AEManaStack pulled = network
                .extractItems(new AEManaStack(this.calculateAmountToSend()), Actionable.MODULATE, this.source);
            if (pulled == null || pulled.getStackSize() <= 0) {
                return TickRateModulation.SLOWER;
            }
            final AEManaStack rest = external.injectItems(pulled, Actionable.MODULATE, this.source);
            final long leftover = rest == null ? 0L : rest.getStackSize();
            if (leftover > 0L) {
                network.injectItems(new AEManaStack(leftover), Actionable.MODULATE, this.source);
            }
            return pulled.getStackSize() > leftover ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } catch (GridAccessException ignored) {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy()
            .isActive();
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager()
            .getSetting(Settings.REDSTONE_CONTROLLED);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getFaceIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon());
        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderInventoryBox(renderer);
        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderInventoryBox(renderer);
        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getFaceIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon());
        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);
        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderBlock(x, y, z, renderer);
        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderBlock(x, y, z, renderer);
        rh.setTexture(
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getFaceIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon());
        rh.setBounds(6, 6, 11, 10, 10, 12);
        rh.renderBlock(x, y, z, renderer);
        this.renderLights(x, y, z, rh, renderer);
    }
}
