package com.asdflj.ae2thing.api.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ConfigurationManager {

    private static final Map<Class<?>, Map<Property, GuiEntry>> entries = new LinkedHashMap<>();

    private ConfigurationManager() {}

    @SideOnly(Side.CLIENT)
    public static void setClientBoolean(Class<?> type, String fieldName, boolean value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            Map<Property, GuiEntry> properties = entries.get(type);
            Property property = properties == null ? null
                : properties.keySet()
                    .stream()
                    .filter(
                        p -> p.getName()
                            .equals(ConfigFieldParser.getFieldName(field)))
                    .findFirst()
                    .orElse(null);
            if (field.getType() != boolean.class || property == null
                || properties.get(property)
                    .synced() != null) {
                throw new IllegalArgumentException("Not a registered client boolean: " + fieldName);
            }
            property.set(value);
            properties.get(property)
                .update()
                .run();
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(fieldName, e);
        }
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public static List<IConfigElement> getConfigElements(Class<?> type) throws ConfigException {
        Map<Property, GuiEntry> properties = entries.get(type);
        if (properties == null) throw new ConfigException("Unregistered config: " + type.getName());
        List<IConfigElement> result = new ArrayList<>();
        properties.forEach((property, entry) -> result.add(new ConfigElement<>(property) {

            @Override
            public String getComment() {
                return entry.comment();
            }

            @Override
            public Object get() {
                return entry.isSynced() ? entry.synced()
                    .displayValue() : super.get();
            }

            @Override
            public boolean isDefault() {
                return entry.isSynced() ? get().equals(getDefault()) : super.isDefault();
            }

            @Override
            public void setToDefault() {
                if (!entry.isSynced()) super.setToDefault();
            }

            @Override
            public void set(Object value) {
                if (entry.isSynced()) return;
                super.set(value);
                entry.update()
                    .run();
            }
        }));
        return result;
    }

    public static void registerConfig(Class<?> type, Configuration file) {
        Map<Property, GuiEntry> properties = new LinkedHashMap<>();
        Config config = type.getAnnotation(Config.class);
        if (config == null) throw new IllegalArgumentException("Missing @Config on " + type.getName());
        String category = config.category()
            .toLowerCase(Locale.ROOT);
        Config.Comment comment = type.getAnnotation(Config.Comment.class);
        if (comment != null) file.addCustomCategoryComment(category, String.join("\n", comment.value()));
        Config.LangKey lang = type.getAnnotation(Config.LangKey.class);
        if (lang != null) file.setCategoryLanguageKey(category, lang.value());
        Config.Sync classSync = type.getAnnotation(Config.Sync.class);
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (field.isSynthetic() || Modifier.isFinal(modifiers)) continue;
            if (!Modifier.isStatic(modifiers))
                throw new IllegalArgumentException("Expected static config field: " + field);
            field.setAccessible(true);
            Config.LangKey fieldLang = field.getAnnotation(Config.LangKey.class);
            String key = fieldLang == null ? config.modid() + "." + category + "." + field.getName()
                : fieldLang.value();
            try {
                ConfigFieldParser.loadField(null, field, file, category, key);
            } catch (ConfigException ex) {
                throw new IllegalStateException(ex);
            }
            Property property = file.getCategory(category)
                .get(ConfigFieldParser.getFieldName(field));
            String initial = property.getDefault();
            Runnable restore = () -> {
                try {
                    ConfigFieldParser.loadField(null, field, file, category, key, initial);
                } catch (ConfigException ex) {
                    throw new IllegalStateException(ex);
                }
            };
            Config.Sync sync = field.getAnnotation(Config.Sync.class);
            SyncedConfigElement element = null;
            if (sync != null ? sync.value() : classSync != null && classSync.value()) {
                property.setRequiresWorldRestart(true);
                element = new SyncedConfigElement(field, restore);
                ConfigSyncHandler.register(element);
            }
            properties.put(property, new GuiEntry(ConfigFieldParser.getComment(field), element, () -> {
                restore.run();
                if (file.getConfigFile() != null) file.save();
            }));
        }
        entries.put(type, properties);
        if (file.hasChanged() && file.getConfigFile() != null) file.save();
    }

    private record GuiEntry(String comment, SyncedConfigElement synced, Runnable update) {

        boolean isSynced() {
            return synced != null && synced.isSynced();
        }
    }
}
