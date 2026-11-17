package com.asdflj.ae2thing.common.item;

import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.me.InfinityCellContentsCache;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.util.ItemStackNBT;
import com.asdflj.ae2thing.util.NameConst;
import com.asdflj.ae2thing.util.Util;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.core.localization.GuiText;
import appeng.tile.inventory.IAEStackInventory;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fox.spiteful.avaritia.render.IHaloRenderItem;

@Optional.Interface(modid = "Avaritia", iface = "fox.spiteful.avaritia.render.IHaloRenderItem")
public class ItemInfinityStorageUniversalCell extends BaseInfinityCellItem<ItemInfinityStorageUniversalCell>
    implements IHaloRenderItem {

    public IIcon halo;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);

    public ItemInfinityStorageUniversalCell() {
        super(NameConst.ITEM_INFINITY_UNIVERSAL_CELL, 2);
    }

    @Override
    public boolean isBlackListed(IAEStack<?> requestedAddition) {
        return requestedAddition == null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private static IAEStack<?> firstContent(ItemStack cell) {
        for (final IAEStackType<?> type : Util.getTypes()) {
            final List<IAEStack<?>> contents = InfinityCellContentsCache.contents(cell, type);
            if (!contents.isEmpty()) return contents.getFirst();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
        boolean displayMoreInfo) {
        final NumberFormat fmt = NUMBER_FORMAT;
        final int bytesPerType = this.getBytesPerType(stack);

        long usedBytes = 0;
        long usedTypes = 0;
        for (IAEStackType<?> type : Util.getTypes()) {
            NBTTagCompound section = InfinityStorageCellSnapshot.section(stack, type);
            if (section == null) continue;
            long types = section.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES);
            long count = section.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT);
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

        final IAEStackInventory config = this.getConfigAEInventory(stack);
        final List<String> filters = new ArrayList<>();
        for (int i = 0; i < config.getSizeInventory(); i++) {
            final IAEStack<?> aes = config.getAEStackInSlot(i);
            if (aes != null) filters.add(aes.getDisplayName());
        }

        if (!filters.isEmpty()) {
            final IInventory upgrades = this.getUpgradesInventory(stack);
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
            for (final IAEStackType<?> type : Util.getTypes()) {
                final NBTTagCompound s = InfinityStorageCellSnapshot.section(stack, type);
                if (s == null) continue;
                long count = s.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT),
                    types = s.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES);
                if (count <= 0 && types <= 0) continue;
                final String unit = type.getDisplayUnit();
                lines.add(type.getDisplayName() + ": " + fmt.format(count) + (unit.isEmpty() ? "" : " " + unit));
            }
            final String uuid = ItemStackNBT.getString(stack, Constants.DISKUUID);
            if (!uuid.isEmpty()) lines.add(EnumChatFormatting.DARK_GRAY + uuid);
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
