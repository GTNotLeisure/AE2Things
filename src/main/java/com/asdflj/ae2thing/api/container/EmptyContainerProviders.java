package com.asdflj.ae2thing.api.container;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.asdflj.ae2thing.common.AE2ThingConfig;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import cpw.mods.fml.common.registry.GameRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public final class EmptyContainerProviders {

    private static final Map<IAEStackType<?>, IEmptyContainerProvider> REGISTRY = new IdentityHashMap<>();
    private static final Object2ObjectMap<String, ObjectList<ItemStack>> configured = new Object2ObjectOpenHashMap<>();
    private static String previousSettings;

    private static synchronized ObjectList<ItemStack> configuredContainers(IAEStackType<?> type) {
        String settings = AE2ThingConfig.defaultContainers;
        if (!settings.equals(previousSettings)) {
            configured.clear();
            for (String entry : settings.split(",")) {
                String[] pair = entry.trim()
                    .split("=", 2);
                if (pair.length != 2) continue;
                String[] spec = pair[1].trim()
                    .split("@", 2);
                String[] id = spec[0].split(":", 2);
                if (id.length != 2) continue;
                Item item = GameRegistry.findItem(id[0], id[1]);
                if (item == null) continue;
                try {
                    int meta = spec.length == 2 ? Integer.parseInt(spec[1]) : 0;
                    if (meta >= 0) configured.computeIfAbsent(pair[0].trim(), _ -> new ObjectArrayList<>())
                        .add(new ItemStack(item, 1, meta));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed entries individually so other stack types still work.
                }
            }
            previousSettings = settings;
        }
        return configured.getOrDefault(type.getId(), ObjectLists.emptyList());
    }

    private EmptyContainerProviders() {}

    public static void register(IAEStackType<?> type, IEmptyContainerProvider provider) {
        REGISTRY.put(type, provider);
    }

    public static boolean isSupported(@Nullable IAEStackType<?> type) {
        return type != null && (REGISTRY.containsKey(type) || !configuredContainers(type).isEmpty());
    }

    @Nullable
    public static IEmptyContainerProvider get(@Nullable IAEStackType<?> type) {
        return type == null ? null : REGISTRY.get(type);
    }

    @Nullable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ItemStack create(@Nullable IAEStack<?> target) {
        if (target == null) return null;
        IAEStackType type = target.getStackType();
        for (ItemStack template : configuredContainers(type)) {
            if (!type.isContainerItemForType(template)) continue;
            IAEStack<?> contents = type.getStackFromContainerItem(template.copy());
            if ((contents == null || contents.getStackSize() == 0) && type.fillContainer(template.copy(), target.copy())
                .rightLong() > 0) return template.copy();
        }
        final IEmptyContainerProvider provider = REGISTRY.get(type);
        return provider == null ? null : provider.create(target);
    }

    public static boolean allowsNetworkFallback(@Nullable IAEStackType<?> type) {
        final IEmptyContainerProvider provider = get(type);
        return provider != null && provider.allowNetworkFallback();
    }
}
