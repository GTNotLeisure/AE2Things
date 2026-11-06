package com.vcwdfca.appbot;

import com.asdflj.ae2thing.Tags;
import com.vcwdfca.appbot.item.ItemCreativeManaCell;
import com.vcwdfca.appbot.item.ItemManaCell;
import com.vcwdfca.appbot.item.ItemManaCellHousing;
import com.vcwdfca.appbot.item.ItemManaExportBus;
import com.vcwdfca.appbot.item.ItemManaImportBus;
import com.vcwdfca.appbot.item.ItemManaMarker;
import com.vcwdfca.appbot.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = AppliedBotanics.MODID,
    name = AppliedBotanics.NAME,
    version = Tags.VERSION,
    dependencies = "required-after:appliedenergistics2;after:Botania",
    acceptedMinecraftVersions = "[1.7.10]")
public class AppliedBotanics {

    public static final String MODID = "ae2thing_ab";
    public static final String NAME = "Applied Botanics";

    @SidedProxy(
        modId = MODID,
        clientSide = "com.vcwdfca.appbot.proxy.ClientProxy",
        serverSide = "com.vcwdfca.appbot.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static ItemManaImportBus MANA_IMPORT_BUS;
    public static ItemManaExportBus MANA_EXPORT_BUS;
    public static ItemManaMarker MANA_MARKER;
    public static ItemManaCellHousing MANA_CELL_HOUSING;
    public static ItemManaCell MANA_CELL_1K;
    public static ItemManaCell MANA_CELL_4K;
    public static ItemManaCell MANA_CELL_16K;
    public static ItemManaCell MANA_CELL_64K;
    public static ItemManaCell MANA_CELL_256K;
    public static ItemManaCell[] MANA_CELLS;
    public static ItemCreativeManaCell MANA_CELL_CREATIVE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MANA_MARKER = new ItemManaMarker();
        MANA_CELL_HOUSING = new ItemManaCellHousing();
        MANA_CELL_1K = new ItemManaCell("mana_cell_1k", 1, 0.5D);
        MANA_CELL_4K = new ItemManaCell("mana_cell_4k", 4, 1.0D);
        MANA_CELL_16K = new ItemManaCell("mana_cell_16k", 16, 1.5D);
        MANA_CELL_64K = new ItemManaCell("mana_cell_64k", 64, 2.0D);
        MANA_CELL_256K = new ItemManaCell("mana_cell_256k", 256, 2.5D);
        MANA_CELL_CREATIVE = new ItemCreativeManaCell();
        MANA_CELLS = new ItemManaCell[] { MANA_CELL_1K, MANA_CELL_4K, MANA_CELL_16K, MANA_CELL_64K, MANA_CELL_256K };

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
