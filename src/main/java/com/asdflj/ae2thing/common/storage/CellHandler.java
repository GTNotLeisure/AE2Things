package com.asdflj.ae2thing.common.storage;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.asdflj.ae2thing.common.item.IItemInventoryHandler;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class CellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        if (is == null || !(is.getItem() instanceof IItemInventoryHandler)) return false;
        if (is.getItem() instanceof ItemBackpackTerminal) return false;
        return !(is.getItem() instanceof ItemInfinityStorageUniversalCell);
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider container, IAEStackType<?> stackType) {
        try {
            // The backpack terminal inventory is backed by a specific player's carried backpacks. Drives, chests and
            // generic cell probes do not have that player context and must not construct it as a storage cell.
            // The universal cell mounts through UniversalCellHandler and the drive/chest mixins instead.
            if (is == null || is.getItem() instanceof ItemBackpackTerminal
                || is.getItem() instanceof ItemInfinityStorageUniversalCell) return null;
            if (is.getItem() instanceof IItemInventoryHandler iih) {
                if (Objects.equals(iih.getStackType(), stackType)) {
                    return iih.getInventoryHandler(is, container, null);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler,
        IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
        if (chest instanceof TileEntity te) {
            Platform.openGUI(player, te, chest.getUp(), GuiBridge.GUI_ME);
        }
    }

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof final CellInventoryHandler ci) {
            return ci.getStatusForCell();
        }else if (handler instanceof final FluidCellInventoryHandler ci) {
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof CellInventoryHandler ci) {
            return ci.getCellInv()
                .getIdleDrain(is);
        } else if (handler instanceof FluidCellInventoryHandler ci) {
            return ci.getCellInv()
                .getIdleDrain(is);
        }
        return 0;
    }
}
