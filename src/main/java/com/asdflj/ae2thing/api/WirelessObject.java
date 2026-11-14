package com.asdflj.ae2thing.api;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemWirelessConnectorTerminal;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.InvUtil;
import com.gtnewhorizon.gtnhlib.item.ItemStackNBT;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.localization.PlayerMessages;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class WirelessObject implements IActionHost {

    private final ItemStack item;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final EntityPlayer player;
    private IGridNode gridNode;
    private IGrid grid;
    private IEnergySource energySource;
    private IMEMonitor<IAEItemStack> itemInv;
    private IMEMonitor<IAEFluidStack> fluidInv;
    private WirelessTerminal wirelessTerminal;
    private PlayerSource source;

    public WirelessObject(ItemStack item, World world, int x, int y, int z, EntityPlayer player)
        throws AppEngException {
        this.item = item;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
        if (Platform.isServer()) {
            this.gridNode = getWirelessGrid();
            if (gridNode == null || !rangeCheck()) {
                throw new AppEngException(
                    PlayerMessages.OutOfRange.toChat()
                        .toString());
            }
            this.grid = this.gridNode.getGrid();
            IStorageGrid iStorageGrid = this.getGrid()
                .getCache(IStorageGrid.class);
            this.itemInv = iStorageGrid.getItemInventory();
            this.fluidInv = iStorageGrid.getFluidInventory();
            this.source = new PlayerSource(this.player, this);
        }
    }

    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this.itemInv;
    }

    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this.fluidInv;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public IGridNode getGridNode() {
        return gridNode;
    }

    public int getX() {
        return x;
    }

    public int getSlot() {
        return PlayerInvType.slotOf(x);
    }

    public ItemStack getStack() {
        return InvUtil.readSlotRef(this.player, x);
    }

    public void setStack(ItemStack stack) {
        InvUtil.writeSlotRef(this.player, x, stack);
    }

    public IGrid getGrid() {
        return grid;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setEnergySource(IEnergySource energySource) {
        this.energySource = energySource;
    }

    public boolean rangeCheck() {
        if (item == null || item.getItem() == null || gridNode == null || gridNode.getGrid() == null) return false;

        Item i = item.getItem();
        if (i instanceof ItemWirelessConnectorTerminal && Config.wirelessConnectorTerminalInfinityConnectionRange)
            return true;

        IWirelessTermHandler handler = AEApi.instance()
            .registries()
            .wireless()
            .getWirelessTerminalHandler(item);
        if (handler != null && handler.hasInfinityRange(item)) return true;

        for (IGridNode node : this.gridNode.getGrid()
            .getMachines(TileWireless.class)) {
            if (!(node.getMachine() instanceof IWirelessAccessPoint accessPoint)) continue;
            if (!accessPoint.isActive()) continue;

            DimensionalCoord location = accessPoint.getLocation();
            if (location.getDimension() != player.dimension) continue;

            double dx = location.x - player.posX;
            double dy = location.y - player.posY;
            double dz = location.z - player.posZ;
            double range = accessPoint.getRange();
            if (dx * dx + dy * dy + dz * dz <= range * range) return true;
        }
        return false;
    }

    public Object getInventory(Class<?> obj)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (wirelessTerminal == null) {
            this.wirelessTerminal = (WirelessTerminal) obj.getConstructor(WirelessObject.class)
                .newInstance(this);
        }
        return wirelessTerminal;
    }

    public IGridNode getWirelessGrid() {
        if (item == null || item.getItem() == null) return null;

        IWirelessTermHandler handler = AEApi.instance()
            .registries()
            .wireless()
            .getWirelessTerminalHandler(item);
        if (handler == null) return null;

        String key = handler.getEncryptionKey(item);
        if (key == null || key.isEmpty()) return null;

        final long parsedKey;
        try {
            parsedKey = Long.parseLong(key);
        } catch (NumberFormatException e) {
            return null;
        }

        Object locatable = AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(parsedKey);
        if (!(locatable instanceof IGridHost host)) return null;

        return host.getGridNode(ForgeDirection.UNKNOWN);
    }

    private void closeGui() {
        this.player.addChatMessage(PlayerMessages.DeviceNotPowered.toChat());
        this.player.closeScreen();
    }

    public int extractPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier, int ticks) {
        if (this.hasEnergyCard()) return (int) amt;
        if (mode == Actionable.SIMULATE) return ticks;
        if (ticks < 10) {
            ticks++;
            return ticks;
        }
        if (this.energySource == null) {
            closeGui();
            return 0;
        }
        double result = energySource.extractAEPower(amt, mode, usePowerMultiplier);
        if (result == 0) closeGui();
        this.setStack(getItemStack());
        ticks = 0;
        return ticks;
    }

    public static boolean hasInfinityBoosterCard(ItemStack item) {
        ItemStackNBT tag = ItemStackNBT.of(item);
        return hasInfinityBoosterCard(tag);
    }

    private static boolean hasInfinityBoosterCard(ItemStackNBT data) {
        return data.hasKey(Constants.INFINITY_BOOSTER_CARD) && data.getBoolean(Constants.INFINITY_BOOSTER_CARD);
    }

    public boolean hasInfinityBoosterCard() {
        return hasInfinityBoosterCard(item);
    }

    public static boolean hasEnergyCard(ItemStack is) {
        ItemStackNBT data = ItemStackNBT.of(is);
        return hasEnergyCard(data);
    }

    private static boolean hasEnergyCard(ItemStackNBT data) {
        return data.hasKey(Constants.INFINITY_ENERGY_CARD) && data.getBoolean(Constants.INFINITY_ENERGY_CARD);
    }

    public boolean hasEnergyCard() {
        return hasEnergyCard(item);
    }

    @Override
    public IGridNode getActionableNode() {
        return this.gridNode;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.gridNode;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN)
            .getMachine()
            .securityBreak();
    }

    public PlayerSource getSource() {
        return this.source;
    }
}
