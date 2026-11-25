package com.asdflj.ae2thing.common.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.localization.Tooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.ItemCellLinkInventory;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.IItemInventory;
import com.asdflj.ae2thing.nei.NEIUtils;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.ItemStackNBT;

public abstract class BaseCellItem extends BaseItem implements IItemInventory, IBaseCellItem, IItemInventoryHandler {

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemCellLinkInventory(player.inventory.mainInventory[x], player, x);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        if (player.isSneaking()) {
            if (ItemStackNBT.getBoolean(item, Constants.LINKED)) {
                item.setTagCompound(null);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, item);
            }
        } else if (AE2ThingConfig.cellLink) {
            InventoryHandler.openGui(
                player,
                w,
                new BlockPos(player.inventory.currentItem, 0, 0),
                ForgeDirection.UNKNOWN,
                this.guiGuiType());
        }
        return super.onItemRightClick(item, w, player);
    }

    private GuiType guiGuiType() {
        return GuiType.CELL_LINK;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        if (GuiScreen.isShiftKeyDown()) {
            addToolTips(stack, player, lines, displayMoreInfo);
        } else {
            lines.add(Tooltips.shift_for_more.getLocal());
        }
    }

    @Override
    public void addToolTips(ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo) {
        if (!AE2ThingConfig.cellLink) {
            lines.add(Tooltips.cell_link_disabled.getLocal());
            return;
        }
        ItemStackNBT data = ItemStackNBT.of(stack);
        if (data.getBoolean(Constants.LINKED)) lines.add(Tooltips.linked.getLocal());
        lines.addAll(
            Arrays.asList(
                Tooltips.cell_link_desc.getLocal()
                    .split("\\\\n")));
        if (Mods.NOT_ENOUGH_ITEMS.isModLoaded()) {
            lines.add(Tooltips.for_full_contents.getLocal(NEIUtils.getUsageKey()));
        }
    }
}
