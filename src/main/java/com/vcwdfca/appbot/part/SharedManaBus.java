package com.vcwdfca.appbot.part;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;
import com.vcwdfca.appbot.ae.AEManaStack;
import com.vcwdfca.appbot.ae.AEManaStackType;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartUpgradeable;
import appeng.util.Platform;

public abstract class SharedManaBus extends PartUpgradeable implements IGridTickable {

    private boolean lastRedstone;

    public SharedManaBus(ItemStack is) {
        super(is);
        this.getConfigManager()
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager()
            .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
    }

    public abstract IIcon getFaceIcon();

    @SuppressWarnings("unchecked")
    protected IMEMonitor<AEManaStack> manaInventory() throws GridAccessException {
        return (IMEMonitor<AEManaStack>) this.getProxy()
            .getStorage()
            .getMEMonitor(AEManaStackType.MANA_STACK_TYPE);
    }

    @Override
    public void upgradesChanged() {
        this.updateState();
    }

    @Override
    public void onNeighborChanged() {
        this.updateState();
        if (this.lastRedstone != this.getHost()
            .hasRedstone(this.getSide())) {
            this.lastRedstone = !this.lastRedstone;
            if (this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE) {
                this.doBusWork();
            }
        }
    }

    private void updateState() {
        try {
            if (!this.isSleeping()) {
                this.getProxy()
                    .getTick()
                    .wakeDevice(
                        this.getProxy()
                            .getNode());
            } else {
                this.getProxy()
                    .getTick()
                    .sleepDevice(
                        this.getProxy()
                            .getNode());
            }
        } catch (final GridAccessException ignored) {}
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        if (player.isSneaking()) {
            return false;
        }
        if (Platform.isServer()) {
            InventoryHandler.openGui(
                player,
                this.getHost()
                    .getTile()
                    .getWorldObj(),
                new BlockPos(
                    this.getHost()
                        .getTile()),
                Objects.requireNonNull(this.getSide()),
                GuiType.MANA_BUS_IO);
        }
        return true;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    protected TileEntity getConnectedTE() {
        TileEntity self = this.getHost()
            .getTile();
        return this.getTileEntity(self, (new BlockPos(self)).getOffSet(this.getSide()));
    }

    private TileEntity getTileEntity(final TileEntity self, final BlockPos pos) {
        final World w = self.getWorldObj();
        if (w.getChunkProvider()
            .chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
            return w.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        }
        return null;
    }

    public int calculateAmountToSend() {
        double amount = 1000D;
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 4:
                amount = amount * 1.5;
            case 3:
                amount = amount * 2;
            case 2:
                amount = amount * 4;
            case 1:
                amount = amount * 8;
        }
        switch (this.getInstalledUpgrades(Upgrades.SUPERSPEED)) {
            case 4:
                amount = amount * 8;
            case 3:
                amount = amount * 12;
            case 2:
                amount = amount * 16;
            case 1:
                amount = amount * 32;
        }
        return (int) Math.floor(amount);
    }

    protected abstract TickRateModulation doBusWork();

    protected abstract boolean canDoBusWork();

    @Override
    public int cableConnectionRenderTo() {
        return 5;
    }
}
