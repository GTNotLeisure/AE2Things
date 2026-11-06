package com.vcwdfca.appbot.item;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.BaseItem;
import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.AppliedBotanics;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemManaCellHousing extends BaseItem {

    public static final String NAME = "mana_cell_housing";

    public ItemManaCellHousing() {
        this.setUnlocalizedName(NAME);
        this.setTextureName(AppliedBotanics.MODID + ":" + NAME);
        this.setMaxStackSize(64);
        this.setCreativeTab(AppBotTabs.INSTANCE);
    }

    public ItemManaCellHousing register() {
        GameRegistry.registerItem(this, NAME, AppliedBotanics.MODID);
        return this;
    }
}
