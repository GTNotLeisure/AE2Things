package com.asdflj.ae2thing.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.CraftingDebugCardObject;
import com.asdflj.ae2thing.api.CraftingDebugHelper;
import com.asdflj.ae2thing.client.localization.Tooltips;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.NameConst;

import appeng.tile.networking.TileController;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCraftingDebugCard extends BaseItem {

    public ItemCraftingDebugCard() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_CRAFTING_DEBUG_CARD);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_CRAFTING_DEBUG_CARD)
                .toString());
    }

    @Override
    public ItemCraftingDebugCard register() {
        GameRegistry.registerItem(this, NameConst.ITEM_CRAFTING_DEBUG_CARD, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (Platform.isServer()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileController tc) {
                CraftingDebugCardObject obj = CraftingDebugHelper.getObject(itemstack);
                obj.sendRecordToPlayer(obj.getGrid(tc), player);
            }
        }
        return super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player) {
        if (player.isSneaking()) {
            CraftingDebugCardObject obj = CraftingDebugHelper.getObject(item);
            obj.setMode(obj.getNextMode());
            if (Platform.isClient()) {
                player.addChatComponentMessage(
                    Tooltips.crafting_debug_card_mode.toChat(
                        obj.getMode()
                            .toChat()));
            }
        }
        return item;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        CraftingDebugCardObject obj = CraftingDebugHelper.getObject(stack);
        lines.add(
            Tooltips.crafting_debug_card_mode.getLocal(
                obj.getMode()
                    .getLocal()));
        if (isShiftKeyDown()) {
            lines.addAll(
                Arrays.asList(
                    Tooltips.crafting_debug_card_desc.getLocal()
                        .split("\\\\n")));
        } else {
            lines.add(Tooltips.shift_for_more.getLocal());
        }
    }
}
