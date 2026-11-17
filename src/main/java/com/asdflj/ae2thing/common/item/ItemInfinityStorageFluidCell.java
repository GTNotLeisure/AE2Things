package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.common.storage.FluidCellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventory;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventoryHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityFluidStorageCellInventory;
import com.asdflj.ae2thing.util.ItemStackNBT;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.api.FluidCraftAPI;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.AppEngException;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.core.localization.GuiText;
import appeng.util.item.AEFluidStackType;

public class ItemInfinityStorageFluidCell extends BaseInfinityCellItem<ItemInfinityStorageFluidCell> {

    public ItemInfinityStorageFluidCell() {
        super(NameConst.ITEM_INFINITY_FLUID_CELL, 0);
    }

    @Override
    public boolean isBlackListed(IAEStack<?> requestedAddition) {
        if (!(requestedAddition instanceof IAEFluidStack fluidStack)) {
            return true;
        }
        return fluidStack.getFluid() == null || FluidCraftAPI.instance()
            .isBlacklistedInStorage(
                fluidStack.getFluid()
                    .getClass());
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEFluidStackType.FLUID_STACK_TYPE;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = ItemStackNBT.getString(is, "FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, AEFluidStackType.FLUID_STACK_TYPE);

        if (inventory instanceof final ITFluidCellInventoryHandler handler) {
            final ITFluidCellInventory cellInventory = handler.getCellInv();
            if (cellInventory != null) {
                String uid = cellInventory.getUUID();
                if (!uid.isEmpty()) lines.add(uid);
                if (handler.isPreformatted()) {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                        : GuiText.Excluded).getLocal();
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());

                    if (GuiScreen.isShiftKeyDown()) {
                        lines.add(GuiText.Filter.getLocal() + ": ");
                        for (IAEFluidStack aeFluidStack : handler.getPartitionInv()) {
                            if (aeFluidStack != null) lines.add("  " + aeFluidStack.getFluidStack().getLocalizedName());
                        }
                    }
                }
            }
        }
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }

    @Override
    public IMEInventoryHandler<IAEFluidStack> getInventoryHandler(ItemStack o, ISaveProvider container,
        EntityPlayer player) throws AppEngException {
        return new FluidCellInventoryHandler(new InfinityFluidStorageCellInventory(o, container));
    }
}
