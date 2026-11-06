package com.vcwdfca.appbot.ae;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.vcwdfca.appbot.tile.TileFluixPool;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStackType;

public class ManaExternalStorageHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, IAEStackType<?> type, BaseActionSource src) {
        return type == AEManaStackType.MANA_STACK_TYPE && !(te instanceof TileFluixPool)
            && ManaExternalInventory.of(te) != null;
    }

    @Override
    public IMEInventory<AEManaStack> getInventory(TileEntity te, ForgeDirection d, IAEStackType<?> type,
        BaseActionSource src) {
        if (type != AEManaStackType.MANA_STACK_TYPE || te instanceof TileFluixPool) {
            return null;
        }
        return ManaExternalInventory.of(te);
    }
}
