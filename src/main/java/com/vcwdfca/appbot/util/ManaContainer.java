package com.vcwdfca.appbot.util;

import java.util.Objects;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.integration.Mods;
import com.vcwdfca.appbot.ae.AEManaStack;

import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import vazkii.botania.api.mana.IManaItem;

public final class ManaContainer {

    private ManaContainer() {}

    public static boolean isManaContainer(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        if (!Mods.BOTANIA.isModLoaded()) return false;
        return stack.getItem() instanceof IManaItem;
    }

    public static AEManaStack getStoredMana(ItemStack stack) {
        if (!isManaContainer(stack)) return null;
        final int mana = ((IManaItem) Objects.requireNonNull(stack.getItem())).getMana(stack);
        return mana > 0 ? new AEManaStack(mana) : null;
    }

    public static long getCapacity(ItemStack stack) {
        if (!isManaContainer(stack)) return 0L;
        return Math.max(((IManaItem) Objects.requireNonNull(stack.getItem())).getMaxMana(stack), 0);
    }

    public static ObjectLongPair<ItemStack> drain(ItemStack container, long amount) {
        if (!isManaContainer(container) || amount <= 0) {
            return new ObjectLongImmutablePair<>(container, 0L);
        }
        final IManaItem item = (IManaItem) Objects.requireNonNull(container.getItem());
        final int stored = item.getMana(container);
        // noinspection MathClampMigration
        final long drained = Math.min(amount, Math.max(stored, 0));
        if (drained <= 0) return new ObjectLongImmutablePair<>(container, 0L);
        final ItemStack out = container.copy();
        out.stackSize = 1;
        item.addMana(out, (int) -drained);
        return new ObjectLongImmutablePair<>(out, drained);
    }

    public static ObjectLongPair<ItemStack> fill(ItemStack container, long amount) {
        if (!isManaContainer(container) || amount <= 0) {
            return new ObjectLongImmutablePair<>(null, 0L);
        }
        final IManaItem item = (IManaItem) Objects.requireNonNull(container.getItem());
        final long space = (long) item.getMaxMana(container) - item.getMana(container);
        final long filled = Math.clamp(space, 0L, amount);
        if (filled <= 0) return new ObjectLongImmutablePair<>(null, 0L);
        final ItemStack out = container.copy();
        out.stackSize = 1;
        item.addMana(out, (int) filled);
        return new ObjectLongImmutablePair<>(out, filled);
    }

    public static ItemStack clear(ItemStack container) {
        if (!isManaContainer(container)) return null;
        final IManaItem item = (IManaItem) Objects.requireNonNull(container.getItem());
        final ItemStack out = container.copy();
        out.stackSize = 1;
        item.addMana(out, -item.getMana(out));
        return out;
    }
}
