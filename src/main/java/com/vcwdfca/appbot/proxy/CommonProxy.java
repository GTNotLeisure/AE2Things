package com.vcwdfca.appbot.proxy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.vcwdfca.appbot.AppliedBotanics;
import com.vcwdfca.appbot.ae.AEManaStackType;
import com.vcwdfca.appbot.ae.CreativeManaCellHandler;
import com.vcwdfca.appbot.ae.ManaCellHandler;
import com.vcwdfca.appbot.ae.ManaExternalStorageHandler;
import com.vcwdfca.appbot.block.BlockFluixPool;
import com.vcwdfca.appbot.item.ItemManaCell;
import com.vcwdfca.appbot.item.ItemManaExportBus;
import com.vcwdfca.appbot.item.ItemManaImportBus;
import com.vcwdfca.appbot.tile.TileFluixPool;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.api.storage.data.AEStackTypeRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void preInit() {
        GameRegistry.registerTileEntity(TileFluixPool.class, AppliedBotanics.MODID + ":" + BlockFluixPool.NAME);
        AEStackTypeRegistry.register(AEManaStackType.MANA_STACK_TYPE);
        AppliedBotanics.MANA_MARKER.register();
        AppliedBotanics.MANA_CELL_HOUSING.register();
        for (ItemManaCell cell : AppliedBotanics.MANA_CELLS) {
            cell.register();
        }
        AppliedBotanics.MANA_CELL_CREATIVE.register();
        AppliedBotanics.MANA_IMPORT_BUS = new ItemManaImportBus().register();
        AppliedBotanics.MANA_EXPORT_BUS = new ItemManaExportBus().register();
    }

    public void init() {}

    public void postInit() {
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(new ManaCellHandler());
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(new CreativeManaCellHandler());
        AEApi.instance()
            .registries()
            .externalStorage()
            .addExternalStorageInterface(new ManaExternalStorageHandler());
        Upgrades.SPEED.registerItem(AppliedBotanics.MANA_EXPORT_BUS.stack(), 4);
        Upgrades.SUPERSPEED.registerItem(AppliedBotanics.MANA_EXPORT_BUS.stack(), 4);
        Upgrades.REDSTONE.registerItem(AppliedBotanics.MANA_EXPORT_BUS.stack(), 1);
        Upgrades.SPEED.registerItem(AppliedBotanics.MANA_IMPORT_BUS.stack(), 4);
        Upgrades.SUPERSPEED.registerItem(AppliedBotanics.MANA_IMPORT_BUS.stack(), 4);
        Upgrades.REDSTONE.registerItem(AppliedBotanics.MANA_IMPORT_BUS.stack(), 1);
        final ItemStack fluidImportBus = ItemAndBlockHolder.FLUID_IMPORT_BUS.stack();
        final ItemStack fluidExportBus = ItemAndBlockHolder.FLUID_EXPORT_BUS.stack();
        GameRegistry.addShapelessRecipe(AppliedBotanics.MANA_IMPORT_BUS.stack(), fluidImportBus);
        GameRegistry.addShapelessRecipe(AppliedBotanics.MANA_EXPORT_BUS.stack(), fluidExportBus);
        GameRegistry.addShapelessRecipe(fluidImportBus, AppliedBotanics.MANA_IMPORT_BUS.stack());
        GameRegistry.addShapelessRecipe(fluidExportBus, AppliedBotanics.MANA_EXPORT_BUS.stack());
        this.registerCellRecipes();
    }

    private void registerCellRecipes() {
        final ItemStack housing = AppliedBotanics.MANA_CELL_HOUSING.stack();
        final IItemDefinition[] parts = { AEApi.instance()
            .definitions()
            .materials()
            .cell1kPart(),
            AEApi.instance()
                .definitions()
                .materials()
                .cell4kPart(),
            AEApi.instance()
                .definitions()
                .materials()
                .cell16kPart(),
            AEApi.instance()
                .definitions()
                .materials()
                .cell64kPart(),
            AEApi.instance()
                .definitions()
                .materials()
                .cell256kPart() };
        for (int i = 0; i < parts.length; i++) {
            final ItemStack part = parts[i].maybeStack(1)
                .orNull();
            if (part != null) {
                GameRegistry.addShapelessRecipe(AppliedBotanics.MANA_CELLS[i].stack(), housing.copy(), part);
            }
        }
        final ItemStack quartzGlass = AEApi.instance()
            .definitions()
            .blocks()
            .quartzGlass()
            .maybeStack(1)
            .orNull();
        final Item manaResource = GameRegistry.findItem("Botania", "manaResource");
        if (quartzGlass != null) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    housing.copy(),
                    "GGG",
                    "G G",
                    "MMM",
                    'G',
                    quartzGlass,
                    'M',
                    new ItemStack(manaResource, 1, 0)));
        }
    }
}
