package com.vcwdfca.appbot.ae;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.exceptions.AppEngException;
import appeng.api.storage.ISaveProvider;
import appeng.me.storage.CellInventory;

public class ManaCellInventory extends CellInventory<AEManaStack> {

    public ManaCellInventory(ItemStack cell, ISaveProvider container) throws AppEngException {
        super(cell, container);
    }

    @Override
    protected AEManaStack readStack(NBTTagCompound tag) {
        return AEManaStack.loadStackFromNBT(tag);
    }

    @Override
    protected String getStackTypeTag() {
        return "manaTypes";
    }

    @Override
    protected String getStackCountTag() {
        return "manaCount";
    }
}
