package com.asdflj.ae2thing.common.item;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.storage.CellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ICellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ITCellInventory;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityItemStorageCellInventory;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.localization.GuiText;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEItemStackType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemInfinityStorageCell extends BaseInfinityCellItem<ItemInfinityStorageCell> implements IItemGroup {

    public ItemInfinityStorageCell() {
        super(NameConst.ITEM_INFINITY_CELL, 2);
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, AEItemStackType.ITEM_STACK_TYPE);

        if (inventory instanceof ICellInventoryHandler handler) {
            final ITCellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                String uid = cellInventory.getUUID();
                if (!uid.isEmpty()) lines.add(uid);
                if (handler.isPreformatted()) {
                    String filter = cellInventory.getOreFilter();
                    if (filter.isEmpty()) {
                        final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST
                            ? GuiText.Included
                            : GuiText.Excluded).getLocal();

                        if (handler.isFuzzy()) {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                        } else {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                        }
                        if (GuiScreen.isShiftKeyDown()) {
                            lines.add(GuiText.Filter.getLocal() + ": ");
                            IAEStackInventory inv = cellInventory.getConfigAEInventory();
                            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                                if (!(inv.getAEStackInSlot(i) instanceof IAEItemStack aes)) {
                                    continue;
                                }
                                ItemStack s = aes.getItemStack();
                                if (s != null) lines.add(s.getDisplayName());
                            }
                        }
                    } else {
                        lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
                    }

                    if (handler.getSticky()) {
                        lines.add(GuiText.Sticky.getLocal());
                    }
                }
            }
        }
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }

    @Override
    public boolean isBlackListed(IAEStack<?> requestedAddition) {
        if (!(requestedAddition instanceof IAEItemStack is)) {
            return true;
        }

        return is.getItem() == null;
    }

    @Override
    public IMEInventoryHandler<IAEItemStack> getInventoryHandler(ItemStack o, ISaveProvider container,
        EntityPlayer player) throws AppEngException {
        return new CellInventoryHandler(new InfinityItemStorageCellInventory(o, container, player));
    }
}
