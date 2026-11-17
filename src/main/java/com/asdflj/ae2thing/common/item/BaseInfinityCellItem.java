package com.asdflj.ae2thing.common.item;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStackType;
import appeng.core.features.AEFeature;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.item.AEItemStackType;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class BaseInfinityCellItem<T extends BaseInfinityCellItem<T>> extends BaseCellItem
    implements IStorageCell {

    protected static final long TOTAL_BYTES = (Long.MAX_VALUE - 9) / 8 + 1;
    protected static final int TOTAL_TYPES = Integer.MAX_VALUE;
    protected static final int BYTES_PER_TYPE = 1;

    private final String name;
    private final int upgradeSlots;

    protected BaseInfinityCellItem(String name, int upgradeSlots) {
        this.name = name;
        this.upgradeSlots = upgradeSlots;
        this.setMaxStackSize(1);
        this.setUnlocalizedName(name);
        this.setTextureName(
            AE2Thing.resource(name)
                .toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T register() {
        GameRegistry.registerItem(this, this.name, AE2Thing.MODID);
        this.setCreativeTab(AE2ThingTabs.INSTANCE);
        return (T) this;
    }

    @Override
    public double getIdleDrain() {
        return 2000D;
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return TOTAL_BYTES;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return BYTES_PER_TYPE;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return BYTES_PER_TYPE;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return TOTAL_TYPES;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return false;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, this.upgradeSlots);
    }

    @Override
    public IAEStackInventory getConfigAEInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.fromItemStack(is);
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        ItemStackNBT.setString(is, "FuzzyMode", fzMode.name());
    }

    @Override
    public IMEInventoryHandler<?> getInventoryHandler(ItemStack o, ISaveProvider container, EntityPlayer player)
        throws AppEngException {
        return null;
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEItemStackType.ITEM_STACK_TYPE;
    }
}
