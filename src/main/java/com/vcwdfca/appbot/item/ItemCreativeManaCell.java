package com.vcwdfca.appbot.item;

import com.asdflj.ae2thing.common.item.BaseItem;
import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.AppliedBotanics;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCreativeManaCell extends BaseItem {

    private static final String NAME = "creative_mana_cell";

    public ItemCreativeManaCell() {
        this.setUnlocalizedName(NAME);
        this.setTextureName(AppliedBotanics.MODID + ":" + NAME);
        this.setMaxStackSize(1);
        this.setCreativeTab(AppBotTabs.INSTANCE);
    }

    @Override
    public ItemCreativeManaCell register() {
        GameRegistry.registerItem(this, NAME, AppliedBotanics.MODID);
        return this;
    }
}
