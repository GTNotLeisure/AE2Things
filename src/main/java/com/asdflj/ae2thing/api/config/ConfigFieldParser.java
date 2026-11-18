package com.asdflj.ae2thing.api.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraftforge.common.config.Configuration;

public final class ConfigFieldParser {

    private static final Parser BOOLEAN = new BooleanParser();
    private static final Parser INTEGER = new IntParser();
    private static final Map<Class<?>, Parser> PARSERS = new HashMap<>();

    static {
        PARSERS.put(boolean.class, BOOLEAN);
        PARSERS.put(Boolean.class, BOOLEAN);
        PARSERS.put(int.class, INTEGER);
        PARSERS.put(Integer.class, INTEGER);
    }

    private ConfigFieldParser() {}

    public static void loadField(@Nullable Object instance, Field field, Configuration config, String category,
        String langKey) throws ConfigException {
        loadField(instance, field, config, category, langKey, null);
    }

    static void loadField(@Nullable Object instance, Field field, Configuration config, String category, String langKey,
        @Nullable String defaultValue) throws ConfigException {
        try {
            getParser(field)
                .load(instance, defaultValue, field, config, category, getFieldName(field), getComment(field), langKey);
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot load " + field, ex);
        }
    }

    public static void saveField(@Nullable Object instance, Field field, Configuration config, String category)
        throws ConfigException {
        try {
            getParser(field).save(instance, field, config, category, getFieldName(field));
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot save " + field, ex);
        }
    }

    private static Parser getParser(Field field) throws ConfigException {
        Parser parser = PARSERS.get(field.getType());
        if (parser == null) throw new ConfigException("No parser for " + field);
        return parser;
    }

    public static boolean canParse(Field field) {
        return PARSERS.containsKey(field.getType());
    }

    public static String getFieldName(Field field) {
        if (field.isAnnotationPresent(Config.Name.class)) {
            return field.getAnnotation(Config.Name.class)
                .value();
        }
        return field.getName();
    }

    static String getComment(Field field) {
        if (field.isAnnotationPresent(Config.Comment.class)) {
            return String.join(
                "\n",
                field.getAnnotation(Config.Comment.class)
                    .value());
        }
        return "";
    }

    static String getValueAsString(@Nullable Object instance, Field field) throws ConfigException {
        try {
            return getParser(field).getAsString(instance, field);
        } catch (Exception e) {
            throw new ConfigException(
                "Failed to get value as string for field " + field.getName()
                    + " of type "
                    + field.getType()
                        .getSimpleName()
                    + " in class "
                    + field.getDeclaringClass()
                        .getName()
                    + ". Caused by: "
                    + e);
        }
    }

    static void setValueFromString(@Nullable Object instance, Field field, String value) throws ConfigException {
        try {
            getParser(field).setFromString(instance, value, field);
        } catch (Exception e) {
            throw new ConfigException(
                "Failed to set value from string for field " + field.getName()
                    + " of type "
                    + field.getType()
                        .getSimpleName()
                    + " in class "
                    + field.getDeclaringClass()
                        .getName()
                    + ". Caused by: "
                    + e);
        }
    }

    public interface Parser {

        void load(@Nullable Object instance, @Nullable String defaultValue, Field field, Configuration config,
            String category, String name, String comment, String langKey) throws IllegalAccessException;

        void save(@Nullable Object instance, Field field, Configuration config, String category, String name)
            throws IllegalAccessException;

        void setFromString(@Nullable Object instance, String value, Field field) throws IllegalAccessException;

        default String getAsString(@Nullable Object instance, Field field) throws IllegalAccessException {
            return field.get(instance)
                .toString();
        }
    }

    private static final class BooleanParser implements Parser {

        @Override
        public void load(Object instance, String defaultValue, Field field, Configuration config, String category,
            String name, String comment, String langKey) throws IllegalAccessException {
            Config.DefaultBoolean annotation = field.getAnnotation(Config.DefaultBoolean.class);
            boolean initial = defaultValue != null ? parse(defaultValue)
                : annotation != null ? annotation.value() : (Boolean) field.get(instance);
            field.set(instance, config.getBoolean(name, category, initial, comment, langKey));
        }

        @Override
        public void save(Object instance, Field field, Configuration config, String category, String name)
            throws IllegalAccessException {
            config.getCategory(category)
                .get(name)
                .set((Boolean) field.get(instance));
        }

        @Override
        public void setFromString(Object instance, String value, Field field) throws IllegalAccessException {
            field.set(instance, parse(value));
        }

        private static boolean parse(String value) {
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value))
                throw new IllegalArgumentException(value);
            return Boolean.parseBoolean(value);
        }
    }

    private static final class IntParser implements Parser {

        @Override
        public void load(Object instance, String defaultValue, Field field, Configuration config, String category,
            String name, String comment, String langKey) throws IllegalAccessException {
            Config.DefaultInt annotation = field.getAnnotation(Config.DefaultInt.class);
            int initial = defaultValue != null ? Integer.parseInt(defaultValue)
                : annotation != null ? annotation.value() : (Integer) field.get(instance);
            Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
            field.set(
                instance,
                config.getInt(
                    name,
                    category,
                    initial,
                    range == null ? Integer.MIN_VALUE : range.min(),
                    range == null ? Integer.MAX_VALUE : range.max(),
                    comment,
                    langKey));
        }

        @Override
        public void save(Object instance, Field field, Configuration config, String category, String name)
            throws IllegalAccessException {
            config.getCategory(category)
                .get(name)
                .set((Integer) field.get(instance));
        }

        @Override
        public void setFromString(Object instance, String value, Field field) throws IllegalAccessException {
            int parsed = Integer.parseInt(value);
            Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
            if (range != null && (parsed < range.min() || parsed > range.max()))
                throw new IllegalArgumentException(value);
            field.set(instance, parsed);
        }
    }
}
