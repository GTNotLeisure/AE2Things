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
    public static boolean cellLink = true;

    @Name("Backpack terminal magnet range")
    @Comment("Backpack terminal magnet range.")
    @RangeInt(min = 8, max = 64)
    public static int magnetRange = 32;

    @Name("Terminal updateView thread")
    @Comment("Update the terminal view on a separate thread.")
    public static boolean updateViewThread = true;

    @Name("Wireless connector terminal infinity connection range")
    @Comment("Allow unlimited wireless connector terminal range.")
    public static boolean wirelessConnectorTerminalInfinityConnectionRange = true;

    @Name("Ex IO Port Transfer Rate")
    @Comment("Base transfer quantity for the extended IO port.")
    @RangeInt(min = 256)
    public static int exIOPortTransferContentsRate = 256;

    @Name("Tic")
    @Comment("Allow the backpack terminal to modify Tinkers' Construct tools.")
    public static boolean backpackTerminalAddTicSupport = false;

    @Name("crafting history size")
    @Comment("Maximum crafting history entries.")
    @RangeInt(min = 100, max = 300)
    public static int craftingHistorySize = 200;

    @Name("Terminal baubles support")
    @Comment("Allow the 2-in-1 interface terminal and backpack terminal in any baubles slot.")
    public static boolean terminalBaubles = true;

    @Name("Modern container interaction in terminal")
    @Comment("Use modern terminal clicks to fill and empty containers.")
    public static boolean modernInteraction = true;

    @Name("Default containers by stack type")
    @Comment("Comma-separated type=mod:item@metadata entries. Repeat a type to try containers in order. Metadata defaults to zero.")
    public static String defaultContainers = "fluid=IC2:itemCellEmpty@0";

    @Name("Multi-pages support")
    @Comment("Processing pattern pages in the 2-in-1 interface terminal (2-64).")
    @RangeInt(min = 2, max = 64)
    public static int multiPagesSupport = 2;

    @Name("Replace hot ingot during pattern transfer")
    @Comment("Replace hot ingot outputs with cooled ingots during processing recipe transfer.")
    public static boolean replaceHotIngot = true;

    @Name("Enhanced pins in terminal")
    @Comment("Enable enhanced native terminal pins. Modern click shortcuts also require modern container interaction.")
    public static boolean enhancedPins = true;

    @Name("Scroll player pins in terminal")
    @Comment("Display player pins at the beginning of the scrolling inventory instead of in a fixed section.")
    @Sync(false)
    public static boolean scrollPlayerPins = false;

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
