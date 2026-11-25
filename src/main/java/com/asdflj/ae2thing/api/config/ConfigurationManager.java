package com.asdflj.ae2thing.api.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class ConfigurationManager {

    private static final Object2ObjectMap<Class<?>, RegisteredConfig> entries = new Object2ObjectOpenHashMap<>();

    private ConfigurationManager() {}

    @SideOnly(Side.CLIENT)
    public static void setClientBoolean(Class<?> type, String fieldName, boolean value) {
        RegisteredConfig config = entries.get(type);
        FieldBinding binding = config == null ? null : config.fields.get(fieldName);
        if (binding == null || binding.parser()
            .getField()
            .getType() != boolean.class || binding.synced() != null)
            throw new IllegalArgumentException("Not a registered client boolean: " + fieldName);
        binding.property()
            .set(value);
        binding.loadLocalValue();
        config.save();
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public static List<IConfigElement> getConfigElements(Class<?> type) throws ConfigException {
        RegisteredConfig config = entries.get(type);
        if (config == null) throw new ConfigException("Unregistered config: " + type.getName());
        List<IConfigElement> result = new ObjectArrayList<>(config.fields.size());
        for (FieldBinding binding : config.fields.values()) {
            result.add(new ConfigElement<>(binding.property()) {

                @Override
                public String getComment() {
                    return binding.parser()
                        .getComment();
                }

                @Override
                public Object get() {
                    return binding.isSynced() ? binding.synced()
                        .displayValue() : super.get();
                }

                @Override
                public boolean isDefault() {
                    return binding.isSynced() ? get().equals(getDefault()) : super.isDefault();
                }

                @Override
                public void setToDefault() {
                    if (binding.isSynced()) return;
                    super.setToDefault();
                    binding.loadLocalValue();
                    config.save();
                }

                @Override
                public void set(Object value) {
                    if (binding.isSynced()) return;
                    super.set(value);
                    binding.loadLocalValue();
                    config.save();
                }
            });
        }
        return result;
    }

    static void withDeferredSave(Class<?> type, Runnable update) {
        RegisteredConfig config = entries.get(type);
        if (config == null) throw new IllegalArgumentException("Unregistered config: " + type.getName());
        config.deferredSaves++;
        try {
            update.run();
        } finally {
            config.deferredSaves--;
            config.save();
        }
    }

    public static void registerConfig(Class<?> type, Configuration file) {
        if (entries.containsKey(type))
            throw new IllegalArgumentException("Already registered config: " + type.getName());
        Config config = type.getAnnotation(Config.class);
        if (config == null) throw new IllegalArgumentException("Missing @Config on " + type.getName());
        List<ConfigFieldParser> parsers = new ObjectArrayList<>();
        ObjectSet<String> names = new ObjectOpenHashSet<>();
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (field.isSynthetic() || Modifier.isFinal(modifiers)) continue;
            if (!Modifier.isStatic(modifiers))
                throw new IllegalArgumentException("Expected static config field: " + field);
            try {
                ConfigFieldParser parser = new ConfigFieldParser(field);
                if (!names.add(parser.getName()))
                    throw new IllegalArgumentException("Duplicate config property: " + parser.getName());
                parsers.add(parser);
            } catch (ConfigException ex) {
                throw new IllegalStateException(ex);
            }
        }
        String category = config.category()
            .toLowerCase(Locale.ROOT);
        Config.Comment comment = type.getAnnotation(Config.Comment.class);
        if (comment != null) file.addCustomCategoryComment(category, String.join("\n", comment.value()));
        Config.LangKey lang = type.getAnnotation(Config.LangKey.class);
        String prefix = lang == null ? config.modid() + "." + category : lang.value();
        if (lang != null) file.setCategoryLanguageKey(category, lang.value());
        Config.Sync classSync = type.getAnnotation(Config.Sync.class);
        RegisteredConfig registered = new RegisteredConfig(file);
        for (ConfigFieldParser parser : parsers) {
            Field field = parser.getField();
            Config.LangKey fieldLang = field.getAnnotation(Config.LangKey.class);
            String key = fieldLang == null ? prefix + "." + field.getName() : fieldLang.value();
            Property property = parser.createProperty(file, category, key);
            Config.Sync sync = field.getAnnotation(Config.Sync.class);
            SyncedConfigElement element = null;
            if (sync != null ? sync.value() : classSync != null && classSync.value()) {
                property.setRequiresWorldRestart(true);
                element = new SyncedConfigElement(parser, () -> loadLocalValue(parser, property));
            }
            FieldBinding binding = new FieldBinding(parser, property, element);
            binding.loadLocalValue();
            registered.fields.put(field.getName(), binding);
        }
        for (FieldBinding binding : registered.fields.values()) {
            if (binding.synced() != null) ConfigSyncHandler.register(binding.synced());
        }
        entries.put(type, registered);
        registered.save();
    }

    private static void loadLocalValue(ConfigFieldParser parser, Property property) {
        try {
            parser.loadLocalValue(property);
        } catch (ConfigException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private record FieldBinding(ConfigFieldParser parser, Property property, SyncedConfigElement synced) {

        boolean isSynced() {
            return synced != null && synced.isSynced();
        }

        void loadLocalValue() {
            ConfigurationManager.loadLocalValue(parser, property);
        }
    }

    private static final class RegisteredConfig {

        private final Configuration file;
        private final Object2ObjectLinkedOpenHashMap<String, FieldBinding> fields = new Object2ObjectLinkedOpenHashMap<>();
        private int deferredSaves;

        private RegisteredConfig(Configuration file) {
            this.file = file;
        }

        private void save() {
            if (deferredSaves == 0 && file.hasChanged() && file.getConfigFile() != null) file.save();
        }
    }
}
