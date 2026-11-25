package com.asdflj.ae2thing.api.config;

final class SyncedConfigElement {

    private final ConfigFieldParser parser;
    private final String id;
    private final Runnable restore;
    private boolean synced;

    SyncedConfigElement(ConfigFieldParser parser, Runnable restore) {
        this.parser = parser;
        this.id = parser.getField()
            .toString();
        this.restore = restore;
    }

    String getId() {
        return id;
    }

    String getValue() throws ConfigException {
        return parser.getValueAsString();
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
        parser.setValueFromString(value);
        synced = true;
    }

    void restoreValue() {
        if (synced) restore.run();
        synced = false;
    }
}
