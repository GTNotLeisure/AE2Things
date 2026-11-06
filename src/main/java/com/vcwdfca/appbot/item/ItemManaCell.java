package com.vcwdfca.appbot.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.asdflj.ae2thing.common.item.BaseItem;
import com.gtnewhorizon.gtnhlib.item.ItemStackNBT;
import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.AppliedBotanics;
import com.vcwdfca.appbot.ae.AEManaStackType;
import com.vcwdfca.appbot.util.ManaFormat;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStackType;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

public class ItemManaCell extends BaseItem implements IStorageCell {

    private final String name;
    private final int kilobytes;
    private final double idleDrain;

    public ItemManaCell(String name, int kilobytes, double idleDrain) {
        this.name = name;
        this.kilobytes = kilobytes;
        this.idleDrain = idleDrain;
        this.setUnlocalizedName(name);
        this.setTextureName(AppliedBotanics.MODID + ":" + name);
        this.setMaxStackSize(1);
        this.setCreativeTab(AppBotTabs.INSTANCE);
    }

    public long getTotalBytes() {
        return (long) this.kilobytes * 1000L;
    }

    public long getMaxMana() {
        return this.getTotalBytes() * AEManaStackType.AMOUNT_PER_BYTE;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    public long getStoredMana(ItemStack stack) {
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, AEManaStackType.MANA_STACK_TYPE);
        if (inventory instanceof ICellInventoryHandler<?>cellHandler && cellHandler.getCellInv() != null) {
            return cellHandler.getCellInv()
                .getStoredItemCount();
        }
        return 0L;
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return (int) this.getTotalBytes();
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return this.getTotalBytes();
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEManaStackType.MANA_STACK_TYPE;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 5);
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
    public String getOreFilter(ItemStack is) {
        return ItemStackNBT.getString(is, "OreFilter");
    }

    @Override
    public ItemManaCell register() {
        GameRegistry.registerItem(this, this.name, AppliedBotanics.MODID);
        return this;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        list.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "ab.mana.cell.stored",
                ManaFormat.format(this.getStoredMana(stack)),
                ManaFormat.format(this.getMaxMana())));
    }
}
