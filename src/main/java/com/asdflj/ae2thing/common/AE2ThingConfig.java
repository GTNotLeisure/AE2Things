package com.asdflj.ae2thing.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.config.Config;
import com.asdflj.ae2thing.api.config.Config.Comment;
import com.asdflj.ae2thing.api.config.Config.LangKey;
import com.asdflj.ae2thing.api.config.Config.Name;
import com.asdflj.ae2thing.api.config.Config.RangeInt;
import com.asdflj.ae2thing.api.config.Config.Sync;
import com.asdflj.ae2thing.api.config.ConfigurationManager;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLInjectionData;

@Config(modid = AE2Thing.MODID, category = AE2Thing.NAME)
@Comment("Settings for AE2Thing.")
@LangKey(AE2ThingConfig.GENERAL_LANG_KEY)
@Sync
public final class AE2ThingConfig {

    public static final String GENERAL_LANG_KEY = "ae2thing.config.general";

    @Name("Enable link cell")
    @Comment("Link cells with the same UUID.")
    @LangKey(GENERAL_LANG_KEY + ".cellLink")
    public static boolean cellLink = true;

    @Name("Backpack terminal magnet range")
    @Comment("Backpack terminal magnet range.")
    @LangKey(GENERAL_LANG_KEY + ".magnetRange")
    @RangeInt(min = 8, max = 64)
    public static int magnetRange = 32;

    @Name("Terminal updateView thread")
    @Comment("Update the terminal view on a separate thread.")
    @LangKey(GENERAL_LANG_KEY + ".updateViewThread")
    public static boolean updateViewThread = true;

    @Name("Wireless connector terminal infinity connection range")
    @Comment("Allow unlimited wireless connector terminal range.")
    @LangKey(GENERAL_LANG_KEY + ".wirelessConnectorTerminalInfinityConnectionRange")
    public static boolean wirelessConnectorTerminalInfinityConnectionRange = true;

    @Name("Ex IO Port Transfer Rate")
    @Comment("Base transfer quantity for the extended IO port.")
    @LangKey(GENERAL_LANG_KEY + ".exIOPortTransferContentsRate")
    @RangeInt(min = 256)
    public static int exIOPortTransferContentsRate = 256;

    @Name("Tic")
    @Comment("Allow the backpack terminal to modify Tinkers' Construct tools.")
    @LangKey(GENERAL_LANG_KEY + ".backpackTerminalAddTicSupport")
    public static boolean backpackTerminalAddTicSupport = false;

    @Name("crafting history size")
    @Comment("Maximum crafting history entries.")
    @LangKey(GENERAL_LANG_KEY + ".craftingHistorySize")
    @RangeInt(min = 100, max = 300)
    public static int craftingHistorySize = 200;

    @Name("Terminal baubles support")
    @Comment("Allow the 2-in-1 interface terminal and backpack terminal in any baubles slot.")
    @LangKey(GENERAL_LANG_KEY + ".terminalBaubles")
    public static boolean terminalBaubles = true;

    @Name("Modern container interaction in terminal")
    @Comment("Use modern terminal clicks to fill and empty containers.")
    @LangKey(GENERAL_LANG_KEY + ".modernInteraction")
    public static boolean modernInteraction = false;

    @Name("Multi-pages support")
    @Comment("Allow the 2-in-1 interface terminal to support more pages.")
    @LangKey(GENERAL_LANG_KEY + ".multiPagesSupport")
    @RangeInt(min = 2, max = 64)
    public static int multiPagesSupport = 2;

    private AE2ThingConfig() {}

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
            Files.deleteIfExists(legacyFile.toPath());
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
        Configuration file = new Configuration(resolveConfigFile());
        migrateTerminalBaublesSetting(file);
        ConfigurationManager.registerConfig(AE2ThingConfig.class, file);
    }

    private static void migrateTerminalBaublesSetting(Configuration file) {
        String category = (AE2Thing.NAME + "." + Constants.DUAL_INTERFACE_TERMINAL).toLowerCase(Locale.ROOT);
        String oldKey = "2-in-1 terminal baubles support";
        if (!file.hasKey(category, oldKey)) return;
        ConfigCategory legacy = file.getCategory(category);
        boolean enabled = legacy.remove(oldKey)
            .getBoolean(true);
        if (!file.hasKey(AE2Thing.NAME.toLowerCase(Locale.ROOT), "Terminal baubles support")) {
            file.get(AE2Thing.NAME, "Terminal baubles support", true)
                .set(enabled);
        }
        if (legacy.isEmpty() && legacy.getChildren()
            .isEmpty()) file.removeCategory(legacy);
    }

}
