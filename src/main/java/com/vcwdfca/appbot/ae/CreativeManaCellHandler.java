package com.vcwdfca.appbot.ae;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.vcwdfca.appbot.item.ItemCreativeManaCell;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeManaCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() instanceof ItemCreativeManaCell;
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider host, IAEStackType<?> type) {
        if (type != AEManaStackType.MANA_STACK_TYPE || !this.isCell(is)) return null;
        return new CreativeManaCellInventory();
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
        return 4;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return 0D;
    }
}
