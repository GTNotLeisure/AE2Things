package com.vcwdfca.appbot.ae;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.vcwdfca.appbot.item.ItemManaCell;

import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ManaCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() instanceof ItemManaCell;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getTopTexture_Light() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getTopTexture_Medium() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getTopTexture_Dark() {
        return null;
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
        if (!(handler instanceof ICellInventoryHandler<?>cellHandler) || cellHandler.getCellInv() == null) {
            return 0;
        }
        if (cellHandler.getCellInv()
            .getStoredItemCount() <= 0) {
            return 1;
        }
        return cellHandler.getCellInv()
            .getFreeBytes() <= 0 ? 4 : 2;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return is != null && is.getItem() instanceof ItemManaCell cell ? cell.getIdleDrain() : 0.5D;
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider host, IAEStackType<?> stackType) {
        if (stackType != AEManaStackType.MANA_STACK_TYPE || !this.isCell(is)) {
            return null;
        }
        try {
            return new ManaCellInventoryHandler(new ManaCellInventory(is, host));
        } catch (AppEngException e) {
            return null;
        }
    }

    @Deprecated
    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider host, StorageChannel chan) {
        return this.getCellInventory(is, host, AEManaStackType.MANA_STACK_TYPE);
    }
}
