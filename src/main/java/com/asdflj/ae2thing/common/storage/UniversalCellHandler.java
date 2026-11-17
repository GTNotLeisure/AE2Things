package com.asdflj.ae2thing.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.asdflj.ae2thing.common.item.ItemInfinityStorageUniversalCell;

import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;

public class UniversalCellHandler implements ICellHandler {

    private static <T extends IAEStack<T>> IMEInventoryHandler<T> build(ItemStack cell, ISaveProvider host,
        IAEStackType<T> type) {
        try {
            return new UniversalCellInventoryHandler<>(new UniversalCellInventory<>(cell, host, type), type);
        } catch (AppEngException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static IMEInventoryHandler<?> create(ItemStack cell, ISaveProvider host, IAEStackType<?> type) {
        return build(cell, host, type);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static IMEInventoryHandler<?> create(ItemStack cell, ISaveProvider host, IAEStackType<?> type,
        TileDrive drive) {
        final IMEInventoryHandler handler = build(cell, host, type);
        return handler == null ? null : new DriveCellHandler<>(handler, type, drive);
    }

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() instanceof ItemInfinityStorageUniversalCell;
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
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        if (handler instanceof ICellInventory<?>inventory) {
            return inventory.getStatusForCell();
        }
        if (handler instanceof CellInventoryHandler<?>cellHandler && cellHandler.getCellInv() != null) {
            return cellHandler.getStatusForCell();
        }
        return 1;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return 0;
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider host, IAEStackType<?> stackType) {
        return null;
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider host, StorageChannel chan) {
        return null;
    }
}
