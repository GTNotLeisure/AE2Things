package com.asdflj.ae2thing.api.container;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;

public final class EmptyContainerProviders {

    private static final Map<IAEStackType<?>, IEmptyContainerProvider> REGISTRY = new IdentityHashMap<>();

    private EmptyContainerProviders() {}

    public static void register(IAEStackType<?> type, IEmptyContainerProvider provider) {
        REGISTRY.put(type, provider);
    }

    public static boolean isSupported(@Nullable IAEStackType<?> type) {
        return type != null && REGISTRY.containsKey(type);
    }

    @Nullable
    public static IEmptyContainerProvider get(@Nullable IAEStackType<?> type) {
        return type == null ? null : REGISTRY.get(type);
    }

    @Nullable
    public static ItemStack create(@Nullable IAEStack<?> target) {
        if (target == null) return null;
        final IEmptyContainerProvider provider = REGISTRY.get(target.getStackType());
        return provider == null ? null : provider.create(target);
    }

    public static boolean allowsNetworkFallback(@Nullable IAEStackType<?> type) {
        final IEmptyContainerProvider provider = get(type);
        return provider != null && provider.allowNetworkFallback();
    }
}
