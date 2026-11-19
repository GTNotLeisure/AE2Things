package com.asdflj.ae2thing.coremod.hooker;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.tile.TileExIOPort;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.EssentiaInventoryAdaptor;
import com.asdflj.ae2thing.inventory.ThaumatoriumInventoryAdapter;
import com.asdflj.ae2thing.util.IOPortTransferUtil;
import com.asdflj.ae2thing.util.Util;

import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEStackType;
import appeng.tile.storage.TileIOPort;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import thaumcraft.common.tiles.TileThaumatorium;

public class CoreModHooks {

    public static void getConnectableTile(TileThaumatorium tile, int y, ForgeDirection face) {
        TileEntity cable = tile.getWorldObj()
            .getTileEntity(tile.xCoord + face.offsetX, tile.yCoord + face.offsetY + y, tile.zCoord + face.offsetZ);
        if (cable == null) return;
        IPart part = Platform.getPartFromTE(cable, face.getOpposite());
        if (Mods.THAUMIC_ENERGISTICS.isModLoaded() && part instanceof PartThaumatoriumInterface pti) {
            int ess = pti.takeEssentia(tile.currentSuction, 1, face);
            if (ess > 0) {
                tile.addToContainer(tile.currentSuction, ess);
            }
        }
    }

    public static long getItemsToMove(TileIOPort ioPort, long base) {
        if (ioPort instanceof TileExIOPort) {
            int amountPerUnit = 1;
            for (final IAEStackType<?> type : Util.getTypes()) {
                amountPerUnit = Math.max(amountPerUnit, type.getAmountPerUnit());
            }
            return IOPortTransferUtil.scaleBudget(base, AE2ThingConfig.exIOPortTransferContentsRate, amountPerUnit);
        }
        return base;
    }

}
