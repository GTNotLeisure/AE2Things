package com.vcwdfca.appbot;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.vcwdfca.appbot.item.ItemManaCell;

public class AppBotTabs extends CreativeTabs {

    public static final AppBotTabs INSTANCE = new AppBotTabs(AppliedBotanics.MODID);

    public AppBotTabs(String label) {
        super(label);
    }

    @Override
    public Item getTabIconItem() {
        if (AppliedBotanics.BLOCK_FLUIX_POOL == null) {
            return AppliedBotanics.MANA_MARKER;
        }
        return Item.getItemFromBlock(AppliedBotanics.BLOCK_FLUIX_POOL);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void displayAllReleventItems(List list) {
        if (AppliedBotanics.BLOCK_FLUIX_POOL == null) {
            return;
        }
        list.add(new ItemStack(AppliedBotanics.BLOCK_FLUIX_POOL));
        list.add(new ItemStack(AppliedBotanics.MANA_MARKER));
        list.add(new ItemStack(AppliedBotanics.MANA_CELL_HOUSING));
        for (ItemManaCell cell : AppliedBotanics.MANA_CELLS) {
            list.add(new ItemStack(cell));
        }
        list.add(new ItemStack(AppliedBotanics.MANA_CELL_CREATIVE));
        list.add(new ItemStack(AppliedBotanics.MANA_IMPORT_BUS));
        list.add(new ItemStack(AppliedBotanics.MANA_EXPORT_BUS));
    }
}
