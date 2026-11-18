package com.asdflj.ae2thing.api.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class ConfigLog {

    private static final Logger LOGGER = LogManager.getLogger("AE2Thing Config");

    private ConfigLog() {}

    static void error(String message, Throwable cause) {
        LOGGER.error(message, cause);
    }
}
