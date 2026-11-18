package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * minimal copy of {@link com.gtnewhorizon.gtnhlib.item.ItemStackNBT}
 */
public final class ItemStackNBT {

    @NotNull
    private final ItemStack stack;

    private ItemStackNBT(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    public static ItemStackNBT of(@NotNull ItemStack stack) {
        return new ItemStackNBT(stack);
    }

    // region instance setter methods

    public ItemStackNBT setTag(String key, @NotNull NBTBase value) {
        ensureInitialized();
        stack.getTagCompound()
            .setTag(key, value);
        return this;
    }

    public ItemStackNBT setByte(String key, byte value) {
        ensureInitialized();
        stack.getTagCompound()
            .setByte(key, value);
        return this;
    }

    public ItemStackNBT setInteger(String key, int value) {
        ensureInitialized();
        stack.getTagCompound()
            .setInteger(key, value);
        return this;
    }

    public ItemStackNBT setLong(String key, long value) {
        ensureInitialized();
        stack.getTagCompound()
            .setLong(key, value);
        return this;
    }

    public ItemStackNBT setString(String key, @NotNull String value) {
        ensureInitialized();
        stack.getTagCompound()
            .setString(key, value);
        return this;
    }

    public ItemStackNBT setBoolean(String key, boolean value) {
        ensureInitialized();
        stack.getTagCompound()
            .setBoolean(key, value);
        return this;
    }

    // endregion
    // region instance getter methods

    public byte getByte(String key) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .getByte(key);
        }
        return 0;
    }

    public int getInteger(String key) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .getInteger(key);
        }
        return 0;
    }

    public long getLong(String key) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .getLong(key);
        }
        return 0L;
    }

    public String getString(String key) {
        return getString(this.stack, key);
    }

    @Nullable
    public NBTTagCompound getCompoundTag(String key) {
        if (stack.hasTagCompound()) {
            final NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
                return nbt.getCompoundTag(key);
            }
        }
        return null;
    }

    public boolean getBoolean(String key) {
        return getBoolean(this.stack, key);
    }

    // endregion
    // region instance util methods

    public boolean hasKey(String key) {
        return stack.hasTagCompound() && stack.getTagCompound()
            .hasKey(key);
    }

    public ItemStackNBT removeTag(String key) {
        if (stack.hasTagCompound()) {
            final NBTTagCompound nbt = stack.getTagCompound();
            nbt.removeTag(key);
            if (nbt.hasNoTags()) {
                stack.setTagCompound(null);
            }
        }
        return this;
    }

    public boolean hasNoTags() {
        return !stack.hasTagCompound() || stack.getTagCompound()
            .hasNoTags();
    }

    @Nullable
    public NBTTagCompound copy() {
        return copy(this.stack);
    }

    private void ensureInitialized() {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
    }

    // endregion
    // region static setter methods

    public static void setByte(ItemStack stack, String key, byte value) {
        ensureInitialized(stack);
        stack.getTagCompound()
            .setByte(key, value);
    }

    public static void setString(ItemStack stack, String key, @NotNull String value) {
        ensureInitialized(stack);
        stack.getTagCompound()
            .setString(key, value);
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        ensureInitialized(stack);
        stack.getTagCompound()
            .setBoolean(key, value);
    }

    // endregion
    // region static getter methods

    public static String getString(ItemStack stack, String key) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .getString(key);
        }
        return "";
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .getBoolean(key);
        }
        return false;
    }

    // endregion
    // region static util methods

    public static boolean hasNoTags(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound()
                .hasNoTags();
        }
        return true;
    }

    @Nullable
    public static NBTTagCompound copy(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return (NBTTagCompound) stack.getTagCompound()
                .copy();
        }
        return null;
    }

    /**
     * This method returns the NBTTagCompound attached to the ItemStack, if it is not present it creates one and returns
     * it. This method defeats the whole point of the api, only use if you absolutely need the NBTTagCompound to exist.
     */
    @NotNull
    public static NBTTagCompound get(@NotNull ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    private static void ensureInitialized(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
    }

    // endregion
}
