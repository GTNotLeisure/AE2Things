package com.vcwdfca.appbot.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.asdflj.ae2thing.common.item.BaseItem;
import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.AppliedBotanics;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemManaMarker extends BaseItem {

    public static final String NAME = "mana_marker";

    public ItemManaMarker() {
        this.setUnlocalizedName(NAME);
        this.setTextureName(AppliedBotanics.MODID + ":" + NAME);
        this.setMaxStackSize(1);
        this.setCreativeTab(AppBotTabs.INSTANCE);
    }

    public ItemManaMarker register() {
        GameRegistry.registerItem(this, NAME, AppliedBotanics.MODID);
        return this;
    }

    public static boolean isMarker(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemManaMarker;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("ab.mana.marker.tooltip"));
    }
}
