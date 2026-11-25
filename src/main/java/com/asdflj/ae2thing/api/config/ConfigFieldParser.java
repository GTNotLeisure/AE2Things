package com.asdflj.ae2thing.api.config;

import java.lang.reflect.Field;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

final class ConfigFieldParser {

    private final Field field;
    private final boolean booleanType;
    private final String name;
    private final String comment;
    private final String defaultValue;
    private final int min;
    private final int max;

    ConfigFieldParser(Field field) throws ConfigException {
        this.field = field;
        Class<?> type = field.getType();
        booleanType = type == boolean.class || type == Boolean.class;
        if (!booleanType && type != int.class && type != Integer.class)
            throw new ConfigException("No parser for " + field);
        field.setAccessible(true);
        Config.Name fieldName = field.getAnnotation(Config.Name.class);
        name = fieldName == null ? field.getName() : fieldName.value();
        Config.Comment fieldComment = field.getAnnotation(Config.Comment.class);
        comment = fieldComment == null ? "" : String.join("\n", fieldComment.value());
        Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
        min = range == null ? Integer.MIN_VALUE : range.min();
        max = range == null ? Integer.MAX_VALUE : range.max();
        if (min > max) throw new ConfigException("Invalid integer range for " + field);
        try {
            if (booleanType) {
                Config.DefaultBoolean annotation = field.getAnnotation(Config.DefaultBoolean.class);
                defaultValue = Boolean.toString(annotation == null ? (Boolean) field.get(null) : annotation.value());
            } else {
                Config.DefaultInt annotation = field.getAnnotation(Config.DefaultInt.class);
                int initial = annotation == null ? (Integer) field.get(null) : annotation.value();
                if (initial < min || initial > max)
                    throw new IllegalArgumentException("Default outside integer range: " + initial);
                defaultValue = Integer.toString(initial);
            }
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot read default for " + field, ex);
        }
    }

    Field getField() {
        return field;
    }

    String getName() {
        return name;
    }

    String getComment() {
        return comment;
    }

    Property createProperty(Configuration config, String category, String langKey) {
        Property property;
        if (booleanType) {
            property = config.get(category, name, Boolean.parseBoolean(defaultValue));
            property.comment = comment + " [default: " + defaultValue + "]";
        } else {
            property = config.get(category, name, Integer.parseInt(defaultValue));
            property.setMinValue(min);
            property.setMaxValue(max);
            property.comment = comment + " [range: " + min + " ~ " + max + ", default: " + defaultValue + "]";
        }
        property.setLanguageKey(langKey);
        return property;
    }

    void loadLocalValue(Property property) throws ConfigException {
        try {
            if (booleanType) {
                boolean value = property.getBoolean(Boolean.parseBoolean(defaultValue));
                field.set(null, value);
                if (!property.getString()
                    .equals(Boolean.toString(value))) property.set(value);
            } else {
                int value = Math.max(min, Math.min(max, property.getInt(Integer.parseInt(defaultValue))));
                field.set(null, value);
                if (!property.getString()
                    .equals(Integer.toString(value))) property.set(value);
            }
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot load " + field, ex);
        }
    }

    String getValueAsString() throws ConfigException {
        try {
            return field.get(null)
                .toString();
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot read " + field, ex);
        }
    }

    void setValueFromString(String value) throws ConfigException {
        try {
            if (booleanType) {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value))
                    throw new IllegalArgumentException("Invalid boolean: " + value);
                field.set(null, Boolean.parseBoolean(value));
            } else {
                int parsed = Integer.parseInt(value);
                if (parsed < min || parsed > max) throw new IllegalArgumentException("Outside integer range: " + value);
                field.set(null, parsed);
            }
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ConfigException("Cannot sync " + field + " from " + value, ex);
        }
    }
}
