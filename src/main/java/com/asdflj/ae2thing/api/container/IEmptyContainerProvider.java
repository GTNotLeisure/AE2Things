package com.asdflj.ae2thing.api.container;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.storage.data.IAEStack;

/**
 * Supplies the default empty container that is taken from the network when a player left-clicks a ME storage entry
 * with an empty hand.
 * <p>
 * Providers are registered per {@link appeng.api.storage.data.IAEStackType} through {@link EmptyContainerProviders},
 * mirroring how {@code AEStackTypeRegistry} attaches capabilities to a stack type. Supporting another stack type only
 * requires registering a provider for it, the container interaction code itself stays type agnostic.
 */
public interface IEmptyContainerProvider {

    /**
     * @param target the entry that was clicked in the terminal
     * @return a template for the empty container, or {@code null} if no container can be derived from the entry.
     *         Callers copy the returned stack before use.
     */
    @Nullable
    ItemStack create(IAEStack<?> target);

    /**
     * Whether the network item storage should be searched for an empty container of this type when {@link #create}
     * returns no template. Disabled by default; enable it for types that have no single canonical empty container,
     * e.g. mana.
     */
    default boolean allowNetworkFallback() {
        return false;
    }
}
