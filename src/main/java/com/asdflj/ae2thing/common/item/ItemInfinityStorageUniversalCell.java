package com.asdflj.ae2thing.common.item;

import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.storage.infinityCell.UniversalCellSnapshot;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.util.ItemStackNBT;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fox.spiteful.avaritia.render.IHaloRenderItem;

@Optional.Interface(modid = "Avaritia", iface = "fox.spiteful.avaritia.render.IHaloRenderItem")
public class ItemInfinityStorageUniversalCell extends BaseItem implements IStorageCell, IHaloRenderItem {

    public static final String NBT_DATA = "ucData";
    public static final String NBT_SNAPSHOT_TYPES = "types";
    public static final String NBT_SNAPSHOT_COUNT = "count";
    public static final String NBT_SNAPSHOT_CONTENTS = "contents";
    public static final String NBT_SNAPSHOT_VERSION = "ver";

    public IIcon halo;

    public ItemInfinityStorageUniversalCell() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_INFINITY_UNIVERSAL_CELL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_INFINITY_UNIVERSAL_CELL)
                .toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
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
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return (Long.MAX_VALUE - 9) / 8 + 1;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isBlackListed(IAEStack<?> requestedAddition) {
        return requestedAddition == null;
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
    public double getIdleDrain() {
        return 0D;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public static NBTTagCompound getSnapshot(ItemStack cell, IAEStackType<?> type) {
        return UniversalCellSnapshot.section(cell, type);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private static IAEStack<?> firstContent(ItemStack cell) {
        for (final IAEStackType<?> type : AEStackTypeRegistry.getSortedTypes()) {
            final List<IAEStack<?>> contents = UniversalCellSnapshot.contents(cell, type);
            if (!contents.isEmpty()) return contents.getFirst();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        final NumberFormat fmt = NumberFormat.getInstance(Locale.ENGLISH);
        final int bytesPerType = this.getBytesPerType(stack);

        long usedBytes = 0;
        long usedTypes = 0;
        for (IAEStackType<?> type : AEStackTypeRegistry.getSortedTypes()) {
            NBTTagCompound section = getSnapshot(stack, type);
            if (section == null) continue;
            long types = section.getInteger(NBT_SNAPSHOT_TYPES);
            long count = section.getLong(NBT_SNAPSHOT_COUNT);
            if (types <= 0 && count <= 0) continue;
            usedTypes += types;
            usedBytes += count / Math.max(1, type.getAmountPerByte()) + types * (long) bytesPerType;
        }
        lines.add(WHITE + fmt.format(usedBytes) + GRAY + " " + GuiText.BytesUsed.getLocal());
        lines.add(WHITE + fmt.format(usedTypes) + GRAY + " " + GuiText.Types.getLocal());

        if (usedTypes == 1) {
            IAEStack<?> only = firstContent(stack);
            if (only != null) lines.add(GuiText.Contains.getLocal() + ": " + only.getDisplayName());
        }

        final IInventory upgrades = this.getUpgradesInventory(stack);
        final IAEStackInventory config = this.getConfigAEInventory(stack);
        final List<String> filters = new ArrayList<>();
        for (int i = 0; i < config.getSizeInventory(); i++) {
            final IAEStack<?> aes = config.getAEStackInSlot(i);
            if (aes != null) filters.add(aes.getDisplayName());
        }

        if (!filters.isEmpty()) {
            final String include = hasUpgrade(upgrades, Upgrades.INVERTER) ? GuiText.Excluded.getLocal()
                : GuiText.Included.getLocal();
            final String match = hasUpgrade(upgrades, Upgrades.FUZZY) ? GuiText.Fuzzy.getLocal()
                : GuiText.Precise.getLocal();
            lines.add(GuiText.Partitioned.getLocal() + " - " + include + ' ' + match);
            if (GuiScreen.isShiftKeyDown()) {
                lines.add(GuiText.Filter.getLocal() + ": ");
                lines.addAll(filters);
            }
            if (hasUpgrade(upgrades, Upgrades.STICKY)) lines.add(GuiText.Sticky.getLocal());
        }

        if (GuiScreen.isShiftKeyDown()) {
            for (final IAEStackType<?> type : AEStackTypeRegistry.getSortedTypes()) {
                final NBTTagCompound s = getSnapshot(stack, type);
                if (s == null) continue;
                long count = s.getLong(NBT_SNAPSHOT_COUNT), types = s.getInteger(NBT_SNAPSHOT_TYPES);
                if (count <= 0 && types <= 0) continue;
                final String unit = type.getDisplayUnit();
                lines.add(type.getDisplayName() + ": " + fmt.format(count) + (unit.isEmpty() ? "" : " " + unit));
            }
            final String uuid = ItemStackNBT.getString(stack, Constants.DISKUUID);
            if (!uuid.isEmpty()) lines.add(EnumChatFormatting.DARK_GRAY + uuid);
        } else {
            lines.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }

        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }

    @SideOnly(Side.CLIENT)
    private static boolean hasUpgrade(final IInventory upgrades, final Upgrades upgrade) {
        if (upgrades == null) return false;
        for (int i = 0; i < upgrades.getSizeInventory(); i++) {
            final ItemStack module = upgrades.getStackInSlot(i);
            if (module != null && module.getItem() instanceof IUpgradeModule item && item.getType(module) == upgrade) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemInfinityStorageUniversalCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_INFINITY_UNIVERSAL_CELL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);

        if (Mods.AVARITIA.isModLoaded()) {
            halo = register.registerIcon("avaritia:halo");
        }
    }

    @Override
    @Optional.Method(modid = "Avaritia")
    public boolean drawHalo(ItemStack stack) {
        return true;
    }

    @Override
    @Optional.Method(modid = "Avaritia")
    public IIcon getHaloTexture(ItemStack stack) {
        return halo;
    }

    @Override
    @Optional.Method(modid = "Avaritia")
    public int getHaloSize(ItemStack stack) {
        return 10;
    }

    @Override
    @Optional.Method(modid = "Avaritia")
    public boolean drawPulseEffect(ItemStack stack) {
        return true;
    }

    @Override
    @Optional.Method(modid = "Avaritia")
    public int getHaloColour(ItemStack stack) {
        return 0xFF000000;
    }
}
