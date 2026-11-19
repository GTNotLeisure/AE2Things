package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.localization.Tooltips;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;

import cpw.mods.fml.common.registry.GameRegistry;

public class MaterialItem extends BaseItem {

    private final String name;

    public MaterialItem(String name, int maxStackSize) {
        super();
        this.name = name;
        this.setMaxStackSize(maxStackSize);
        this.setUnlocalizedName(name);
        this.setTextureName(
            AE2Thing.resource(name)
                .toString());
    }

    @Override
    public MaterialItem register() {
        GameRegistry.registerItem(this, this.name, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        lines.add(Tooltips.just_material.getLocal());
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }
}
