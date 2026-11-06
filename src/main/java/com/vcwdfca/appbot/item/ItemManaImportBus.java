package com.vcwdfca.appbot.item;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.asdflj.ae2thing.common.item.BaseItem;
import com.asdflj.ae2thing.util.NameConst;
import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.AppliedBotanics;
import com.vcwdfca.appbot.part.PartManaImportBus;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemManaImportBus extends BaseItem implements IPartItem {

    public ItemManaImportBus() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_MANA_IMPORT);
        AEApi.instance()
            .partHelper()
            .setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartManaImportBus createPartFromItemStack(ItemStack is) {
        return new PartManaImportBus(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float xOffset, float yOffset, float zOffset) {
        return AEApi.instance()
            .partHelper()
            .placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    @Override
    public ItemManaImportBus register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_MANA_IMPORT, AppliedBotanics.MODID);
        this.setCreativeTab(AppBotTabs.INSTANCE);
        return this;
    }

    @Override
    public void registerIcons(IIconRegister _iconRegister) {}

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
