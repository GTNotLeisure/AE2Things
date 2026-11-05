package com.asdflj.ae2thing.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.minecraftforge.common.config.Configuration;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLInjectionData;

public class Config {

    private static final String GENERAL_LANG_KEY = "ae2thing.config.general";
    private static final String DUAL_TERMINAL_CATEGORY = AE2Thing.NAME + "." + Constants.DUAL_INTERFACE_TERMINAL;
    private static final String DUAL_TERMINAL_LANG_KEY = "ae2thing.config.dualInterfaceTerminal";

    private static final Configuration Config = new Configuration(resolveConfigFile());

    public static boolean cellLink;
    public static int magnetRange;
    public static boolean updateViewThread = true;
    public static boolean wirelessConnectorTerminalInfinityConnectionRange = true;
    public static int exIOPortTransferContentsRate;
    public static boolean backpackTerminalAddTicSupport = false;
    public static int craftingHistorySize = 200;
    public static boolean dualTerminalCanStoredInBaubles = false;

    private static File resolveConfigFile() {
        final File configDir = new File((File) FMLInjectionData.data()[6], "config");
        final File configFile = new File(new File(configDir, AE2Thing.MODID), AE2Thing.MODID + ".cfg");
        final File legacyFile = new File(configDir, AE2Thing.MODID + ".cfg");
        if (configFile.isFile() || !legacyFile.isFile()) return configFile;
        try {
            final File parent = configFile.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("cannot create config folder " + parent);
            }
            Files.copy(legacyFile.toPath(), configFile.toPath());
            FMLLog.info("[%s] Migrated config file from %s to %s", AE2Thing.MODID, legacyFile, configFile);
            return configFile;
        } catch (IOException e) {
            FMLLog.warning(
                "[%s] Failed to migrate config file %s to %s, keep using the legacy one: %s",
                AE2Thing.MODID,
                legacyFile,
                configFile,
                e.toString());
            return legacyFile;
        }
    }

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadProperty() {
        cellLink = Config.getBoolean(
            "Enable link cell",
            AE2Thing.NAME,
            true,
            "Enable link Cell,It will link every same uuid cell",
            GENERAL_LANG_KEY + ".cellLink");
        magnetRange = Config.getInt(
            "Backpack terminal magnet range",
            AE2Thing.NAME,
            32,
            8,
            64,
            "Set backpack terminal magnet range",
            GENERAL_LANG_KEY + ".magnetRange");
        updateViewThread = Config.getBoolean(
            "Terminal updateView thread",
            AE2Thing.NAME,
            true,
            "Terminal create update view thread",
            GENERAL_LANG_KEY + ".updateViewThread");
        wirelessConnectorTerminalInfinityConnectionRange = Config.getBoolean(
            "Wireless connector terminal infinity connection range",
            AE2Thing.NAME,
            true,
            "Wireless connector terminal infinity connection range",
            GENERAL_LANG_KEY + ".wirelessConnectorTerminalInfinityConnectionRange");
        exIOPortTransferContentsRate = Config.getInt(
            "Ex IO Port Transfer Rate",
            AE2Thing.NAME,
            256,
            256,
            Integer.MAX_VALUE,
            "Set Ex IO Port Transfer Rate. Base transfer quantity = 256. Make sure you have enough power to transfer stack.",
            GENERAL_LANG_KEY + ".exIOPortTransferContentsRate");
        backpackTerminalAddTicSupport = Config.getBoolean(
            "Tic",
            AE2Thing.NAME,
            false,
            "Let Backpack Terminal can forge tic tool",
            GENERAL_LANG_KEY + ".backpackTerminalAddTicSupport");
        craftingHistorySize = Config.getInt(
            "crafting history size",
            AE2Thing.NAME,
            200,
            100,
            300,
            "crafting history size",
            GENERAL_LANG_KEY + ".craftingHistorySize");
        dualTerminalCanStoredInBaubles = Config.getBoolean(
            "2-in-1 terminal baubles support",
            DUAL_TERMINAL_CATEGORY,
            false,
            "Let the 2-in-1 terminal be stored in baubles slots",
            DUAL_TERMINAL_LANG_KEY + ".canStoredInBaubles");
        if (Config.hasChanged()) Config.save();
    }

    private static void loadCategory() {
        Config.addCustomCategoryComment(AE2Thing.NAME, "Settings for AE2Thing.");
        Config.setCategoryLanguageKey(AE2Thing.NAME, GENERAL_LANG_KEY);
        Config.setCategoryLanguageKey(DUAL_TERMINAL_CATEGORY, DUAL_TERMINAL_LANG_KEY);
        Config.setCategoryRequiresMcRestart(DUAL_TERMINAL_CATEGORY, true);
    }
}
