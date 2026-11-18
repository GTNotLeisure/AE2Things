package com.asdflj.ae2thing.api.config;

import java.lang.reflect.Field;

final class SyncedConfigElement {

    private final Field field;
    private final Runnable restore;
    private boolean synced;

    SyncedConfigElement(Field field, Runnable restore) {
        this.field = field;
        this.restore = restore;
    }

    String getValue() throws ConfigException {
        return ConfigFieldParser.getValueAsString(null, field);
    }

    boolean isSynced() {
        return synced;
    }

    String displayValue() {
        try {
            return getValue();
        } catch (ConfigException ex) {
            throw new IllegalStateException(ex);
        }
    }

    void setSyncValue(String value) throws ConfigException {
        ConfigFieldParser.setValueFromString(null, field, value);
        synced = true;
    }

    void restoreValue() {
        if (synced) restore.run();
        synced = false;
    }

    @Override
    public String toString() {
        return field.toString();
    }
}
