package com.vcwdfca.appbot.ae;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.util.item.MeaningfulAEStackIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ManaList implements IItemList<AEManaStack> {

    private final ObjectOpenHashSet<AEManaStack> records = new ObjectOpenHashSet<>();

    @Override
    public void addStorage(AEManaStack option) {
        if (option == null) return;
        final AEManaStack existing = this.records.get(option);
        if (existing != null) {
            existing.incStackSize(option.getStackSize());
            return;
        }
        this.records.add(option.copy());
    }

    @Override
    public void addCrafting(AEManaStack option) {
        if (option == null) return;
        final AEManaStack existing = this.records.get(option);
        if (existing != null) {
            existing.setCraftable(true);
            return;
        }
        final AEManaStack created = option.copy();
        created.setStackSize(0);
        created.setCraftable(true);
        this.records.add(created);
    }

    @Override
    public void addRequestable(AEManaStack option) {
        if (option == null) return;
        final AEManaStack existing = this.records.get(option);
        if (existing != null) {
            existing.setCountRequestable(existing.getCountRequestable() + option.getCountRequestable());
            return;
        }
        final AEManaStack created = option.copy();
        created.setStackSize(0);
        created.setCraftable(false);
        this.records.add(created);
    }

    @Override
    public AEManaStack getFirstItem() {
        final Iterator<AEManaStack> it = this.iterator();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public int size() {
        return this.records.size();
    }

    @Override
    @NotNull
    public Iterator<AEManaStack> iterator() {
        return new MeaningfulAEStackIterator<>(this.records.iterator());
    }

    @Override
    public void resetStatus() {
        for (AEManaStack stack : this.records) {
            stack.reset();
        }
    }

    @Override
    public @Nullable IAEStackType<AEManaStack> getStackType() {
        return AEManaStackType.MANA_STACK_TYPE;
    }

    @Override
    public void add(AEManaStack option) {
        if (option == null) return;
        final AEManaStack existing = this.records.get(option);
        if (existing != null) {
            existing.add(option);
            return;
        }
        this.records.add(option.copy());
    }

    @Override
    public AEManaStack findPrecise(AEManaStack i) {
        return i == null ? null : this.records.get(i);
    }

    @Override
    public Collection<AEManaStack> findFuzzy(AEManaStack input, FuzzyMode fuzzy) {
        final AEManaStack found = this.findPrecise(input);
        return found == null ? Collections.emptyList() : Collections.singletonList(found);
    }

    @Override
    public boolean isEmpty() {
        return this.records.isEmpty();
    }
}
