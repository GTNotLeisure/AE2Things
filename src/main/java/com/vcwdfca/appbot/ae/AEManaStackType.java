package com.vcwdfca.appbot.ae;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import com.vcwdfca.appbot.client.ManaIcons;
import com.vcwdfca.appbot.item.ItemManaMarker;
import com.vcwdfca.appbot.util.ManaContainer;

import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

public class AEManaStackType implements IAEStackType<AEManaStack> {

    public static final AEManaStackType MANA_STACK_TYPE = new AEManaStackType();
    public static final String MANA_STACK_ID = "mana";
    private static final String unlocalized = "ab.mana.name";
    private static final String unlocalized_unit = "ab.mana.unit";

    public static final int AMOUNT_PER_UNIT = 1_000_000;
    public static final int AMOUNT_PER_BYTE = 500;

    @Override
    public String getId() {
        return MANA_STACK_ID;
    }

    @Override
    public String getDisplayName() {
        return StatCollector.translateToLocal(unlocalized);
    }

    @Override
    public String getDisplayUnit() {
        return StatCollector.translateToLocal(unlocalized_unit);
    }

    @Override
    public AEManaStack loadStackFromNBT(NBTTagCompound tag) {
        return AEManaStack.loadStackFromNBT(tag);
    }

    @Override
    public AEManaStack loadStackFromByte(ByteBuf buffer) {
        return AEManaStack.loadManaStackFromPacket(buffer);
    }

    @Override
    public IItemList<AEManaStack> createList() {
        return new ManaList();
    }

    @Override
    public @Range(from = 1L, to = 2147483647L) int getAmountPerUnit() {
        return AMOUNT_PER_UNIT;
    }

    @Override
    public boolean isContainerItemForType(@Nullable ItemStack container) {
        return ManaContainer.isManaContainer(container);
    }

    @Override
    public @Nullable AEManaStack getStackFromContainerItem(@NotNull ItemStack container) {
        return ManaContainer.getStoredMana(container);
    }

    @Override
    public @Nullable AEManaStack convertStackFromItem(@NotNull ItemStack itemStack) {
        return ItemManaMarker.isMarker(itemStack) ? new AEManaStack() : null;
    }

    @Override
    public long getContainerItemCapacity(@NotNull ItemStack container, @NotNull AEManaStack stack) {
        return ManaContainer.getCapacity(container);
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> drainStackFromContainer(@NotNull ItemStack container,
        @NotNull AEManaStack stack) {
        return ManaContainer.drain(container, stack.getStackSize());
    }

    @Override
    public @Nullable ItemStack clearFilledContainer(@NotNull ItemStack container) {
        return ManaContainer.clear(container);
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> fillContainer(@NotNull ItemStack container, @NotNull AEManaStack stack) {
        return ManaContainer.fill(container, stack.getStackSize());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getButtonTexture() {
        return TextureMap.locationItemsTexture;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getButtonIcon() {
        return ManaIcons.manasteel();
    }

    @Override
    public AEManaStack getTestStack() {
        return new AEManaStack();
    }

    @Override
    public int getAmountPerByte() {
        return AMOUNT_PER_BYTE;
    }
}
