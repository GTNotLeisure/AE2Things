package com.vcwdfca.appbot.tile;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.vcwdfca.appbot.AppliedBotanics;
import com.vcwdfca.appbot.ae.AEManaStack;
import com.vcwdfca.appbot.ae.AEManaStackType;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import vazkii.botania.common.block.tile.mana.TilePool;

public class TileFluixPool extends TilePool implements IGridHost, IGridBlock, IActionHost {

    private static final String TAG_MANA = "abMana";
    private static final String TAG_MANA_CAP = "abManaCap";

    private IGridNode node;
    private int mirroredMana;
    private int mirroredCapacity;
    private final BaseActionSource source = new MachineSource(this);

    public TileFluixPool() {
        this.manaCap = TilePool.MAX_MANA;
    }

    @Override
    public void validate() {
        super.validate();
        if (this.worldObj == null || this.worldObj.isRemote) return;
        if (this.node == null) {
            this.node = AEApi.instance()
                .createGridNode(this);
        }
        this.node.updateState();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.node != null) {
            this.node.destroy();
            this.node = null;
        }
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.node;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {}

    @Override
    public IGridNode getActionableNode() {
        return this.node;
    }

    @Override
    public double getIdlePowerUsage() {
        return 1.0D;
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.Transparent;
    }

    @Override
    public void onGridNotification(GridNotification notification) {}

    @Override
    public void setNetworkStatus(IGrid grid, int channelsInUse) {}

    @Override
    public EnumSet<ForgeDirection> getConnectableSides() {
        return EnumSet.complementOf(EnumSet.of(ForgeDirection.UP));
    }

    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public void gridChanged() {}

    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(AppliedBotanics.BLOCK_FLUIX_POOL);
    }

    @SuppressWarnings("unchecked")
    private IMEMonitor<AEManaStack> monitor() {
        final IGrid grid = this.node == null ? null : this.node.getGrid();
        if (grid == null) return null;
        final IStorageGrid storage = grid.getCache(IStorageGrid.class);
        return storage == null ? null : (IMEMonitor<AEManaStack>) storage.getMEMonitor(AEManaStackType.MANA_STACK_TYPE);
    }

    @Override
    public int getCurrentMana() {
        if (this.worldObj == null || this.worldObj.isRemote) return this.mirroredMana;
        final IMEMonitor<AEManaStack> monitor = this.monitor();
        if (monitor == null) return this.mirroredMana;
        final AEManaStack out = monitor
            .extractItems(new AEManaStack(Integer.MAX_VALUE), Actionable.SIMULATE, this.source);
        return out == null ? 0 : (int) Math.min(out.getStackSize(), Integer.MAX_VALUE);
    }

    @Override
    public void recieveMana(int mana) {
        if (mana == 0) return;
        final IMEMonitor<AEManaStack> monitor = this.monitor();
        if (monitor == null) {
            this.mirroredMana = Math.clamp(this.mirroredMana + mana, 0, TilePool.MAX_MANA);
            this.markDirty();
            return;
        }
        if (mana > 0) {
            monitor.injectItems(new AEManaStack(mana), Actionable.MODULATE, this.source);
        } else {
            monitor.extractItems(new AEManaStack(-mana), Actionable.MODULATE, this.source);
        }
        this.markDirty();
        this.markDispatchable();
    }

    @Override
    public int getAvailableSpaceForMana() {
        final IMEMonitor<AEManaStack> monitor = this.monitor();
        if (monitor == null) return Math.max(0, TilePool.MAX_MANA - this.mirroredMana);
        final AEManaStack rest = monitor
            .injectItems(new AEManaStack(Integer.MAX_VALUE), Actionable.SIMULATE, this.source);
        if (rest == null) return Integer.MAX_VALUE;
        return (int) Math.min(rest.getStackSize(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isFull() {
        return this.getAvailableSpaceForMana() <= 0;
    }

    public int getManaCapacity() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return this.mirroredCapacity;
        }
        return this.computeCapacity();
    }

    private int computeCapacity() {
        final long current = Math.max(0L, this.getCurrentMana());
        final long free = Math.max(0L, this.getAvailableSpaceForMana());
        return (int) Math.min(current + free, Integer.MAX_VALUE);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.worldObj == null || this.worldObj.isRemote) return;
        if (this.node != null) this.node.updateState();
        final int mana = this.getCurrentMana();
        if (mana != this.mirroredMana) {
            this.mirroredMana = mana;
            this.markDispatchable();
        }
        final int capacity = this.computeCapacity();
        if (capacity != this.mirroredCapacity) {
            this.mirroredCapacity = capacity;
            this.markDispatchable();
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound cmp) {
        super.writeCustomNBT(cmp);
        final boolean remote = this.worldObj == null || this.worldObj.isRemote;
        cmp.setInteger(TAG_MANA, remote ? this.mirroredMana : this.getCurrentMana());
        cmp.setInteger(TAG_MANA_CAP, remote ? this.mirroredCapacity : this.computeCapacity());
    }

    @Override
    public void readCustomNBT(NBTTagCompound cmp) {
        super.readCustomNBT(cmp);
        if (cmp.hasKey(TAG_MANA)) this.mirroredMana = cmp.getInteger(TAG_MANA);
        if (cmp.hasKey(TAG_MANA_CAP)) this.mirroredCapacity = cmp.getInteger(TAG_MANA_CAP);
    }
}
