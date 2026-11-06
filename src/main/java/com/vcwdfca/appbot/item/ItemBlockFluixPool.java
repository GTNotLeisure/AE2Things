package com.vcwdfca.appbot.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;

public class ItemBlockFluixPool extends ItemBlock {

    public ItemBlockFluixPool(Block block) {
        super(block);
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.field_150939_a.getIcon(0, meta);
    }
}
