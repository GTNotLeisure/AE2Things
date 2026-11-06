package com.vcwdfca.appbot.ae;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.vcwdfca.appbot.AppliedBotanics;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.item.AEStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class AEManaStack extends AEStack<AEManaStack> {

    private static final String unlocalized = "ab.mana.name";

    private static final String TAG_AMOUNT = "Cnt";
    private static final String TAG_REQUESTABLE = "Req";
    private static final String TAG_CRAFTABLE = "Craft";
    private static final String TAG_REQUESTABLE_CRAFTS = "ReqMade";
    private static final String TAG_USED_PERCENT = "UsedPercent";

    public AEManaStack() {
        this(1);
    }

    public AEManaStack(long amount) {
        this.setStackSize(amount);
    }

    public AEManaStack(AEManaStack other) {
        this.setStackSize(other.getStackSize());
        this.setCountRequestable(other.getCountRequestable());
        this.setCountRequestableCrafts(other.getCountRequestableCrafts());
        this.setCraftable(other.isCraftable());
        this.setUsedPercent(other.getUsedPercent());
    }

    @Override
    protected void writeIdentity(ByteBuf i) {}

    @Override
    protected void readNBT(ByteBuf i) {}

    @Override
    public void add(AEManaStack is) {
        if (is == null) return;
        this.incStackSize(is.getStackSize());
        this.incCountRequestable(is.getCountRequestable());
        this.setCountRequestableCrafts(this.getCountRequestableCrafts() + is.getCountRequestableCrafts());
        this.setCraftable(this.isCraftable() || is.isCraftable());
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setLong(TAG_AMOUNT, this.getStackSize());
        tag.setLong(TAG_REQUESTABLE, this.getCountRequestable());
        tag.setBoolean(TAG_CRAFTABLE, this.isCraftable());
        if (this.getCountRequestableCrafts() != 0L) {
            tag.setLong(TAG_REQUESTABLE_CRAFTS, this.getCountRequestableCrafts());
        }
        if (this.getUsedPercent() != 0f) {
            tag.setFloat(TAG_USED_PERCENT, this.getUsedPercent());
        }
    }

    @Override
    public boolean fuzzyComparison(Object st, FuzzyMode mode) {
        return st instanceof AEManaStack;
    }

    @Override
    public AEManaStack copy() {
        return new AEManaStack(this);
    }

    @Override
    public AEManaStack empty() {
        final AEManaStack dup = this.copy();
        dup.reset();
        return dup;
    }

    @Override
    public IAETagCompound getTagCompound() {
        return null;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public String getLocalizedName() {
        return this.getDisplayName();
    }

    @Override
    public boolean isSameType(AEManaStack stack) {
        return stack != null;
    }

    @Override
    public boolean isSameType(Object stack) {
        return stack instanceof AEManaStack;
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalized;
    }

    @Override
    public String getDisplayName() {
        return StatCollector.translateToLocal(unlocalized);
    }

    @Override
    public String getModId() {
        return AppliedBotanics.MODID;
    }

    @Override
    public void setTagCompound(NBTTagCompound tag) {}

    @Override
    public boolean hasTagCompound() {
        return false;
    }

    @Override
    public @Nullable ItemStack getItemStackForNEI() {
        return AppliedBotanics.MANA_MARKER == null ? null : new ItemStack(AppliedBotanics.MANA_MARKER);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInGui(Minecraft mc, int x, int y) {
        AEManaStackRenderer.drawInGui(x, y);
    }

    @Override
    public void drawOnBlockFace(World world) {}

    @Override
    public int getAmountPerUnit() {
        return AEManaStackType.AMOUNT_PER_UNIT;
    }

    @Override
    public IAEStackType<AEManaStack> getStackType() {
        return AEManaStackType.MANA_STACK_TYPE;
    }

    public static AEManaStack loadStackFromNBT(NBTTagCompound tag) {
        final AEManaStack stack = new AEManaStack(0);
        stack.setStackSize(tag.getLong("Cnt"));
        stack.setCountRequestable(tag.getLong("Req"));
        stack.setCraftable(tag.getBoolean("Craft"));
        stack.setCountRequestableCrafts(tag.getLong("ReqMade"));
        stack.setUsedPercent(tag.getFloat("UsedPercent"));
        return stack;
    }

    public static AEManaStack loadManaStackFromPacket(final ByteBuf data) {
        final byte mask = data.readByte();
        final byte stackType = (byte) ((mask & 0x0C) >> 2);
        final byte countReqType = (byte) ((mask & 0x30) >> 4);
        final boolean isCraftable = (mask & 0x40) > 0;

        final long stackSize = getPacketValue(stackType, data);
        final long countRequestable = getPacketValue(countReqType, data);

        final byte mask2 = data.readByte();
        final byte countReqMadeType = (byte) ((mask2 & 0x3));
        final byte usedPercentType = (byte) ((mask2 & 0xC) >> 2);
        final long countRequestableCrafts = getPacketValue(countReqMadeType, data);
        final long longUsedPercent = getPacketValue(usedPercentType, data);

        final AEManaStack aes = new AEManaStack(stackSize);
        aes.setCountRequestable(countRequestable);
        aes.setCraftable(isCraftable);
        aes.setCountRequestableCrafts(countRequestableCrafts);
        aes.setUsedPercent(longUsedPercent / 10000f);
        return aes;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AEManaStack;
    }

    @Override
    public int hashCode() {
        return AEManaStackType.MANA_STACK_ID.hashCode();
    }
}
