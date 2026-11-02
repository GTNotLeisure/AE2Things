package com.asdflj.ae2thing.loader;

import static com.glodblock.github.loader.ItemAndBlockHolder.CELL_HOUSING;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.integration.ae2stuff.Ae2StuffIntegration;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipeBuilder;

public class GTRecipeLoader implements Runnable {

    public static final GTRecipeLoader INSTANCE = new GTRecipeLoader();

    private static final ItemStack AE2_WIRELESS = AEApi.instance()
        .definitions()
        .blocks()
        .wireless()
        .maybeStack(1)
        .orNull();
    private static final ItemStack AE2_CERTUS = AEApi.instance()
        .definitions()
        .items()
        .certusQuartzWrench()
        .maybeStack(1)
        .orNull();
    private static final ItemStack AE2_SINGULARITY = AEApi.instance()
        .definitions()
        .materials()
        .singularity()
        .maybeStack(1)
        .orNull();
    private static final ItemStack ae2StuffWireless = Ae2StuffIntegration.wirelessBlockStack();
    private static final ItemStack AE2_WIRELESS_HUB = AEApi.instance()
        .definitions()
        .blocks()
        .wirelessHub()
        .maybeStack(1)
        .orNull();
    private static final ItemStack EVERBURN_URN = GameRegistry.findItemStack("ThaumicExploration", "everburnUrn", 1);

    @Override
    public void run() {
        if (ae2StuffWireless != null) {
            GameRegistry
                .addShapelessRecipe(ItemAndBlockHolder.WIRELESS_DISTRIBUTOR.stack(), ae2StuffWireless, AE2_WIRELESS);
        }
        if (AE2_WIRELESS_HUB != null) {
            GameRegistry
                .addShapelessRecipe(ItemAndBlockHolder.WIRELESS_DISTRIBUTOR.stack(), AE2_WIRELESS_HUB, AE2_WIRELESS);
        }
        for (int i = 0; i < AEColor.values().length; i++) {
            GameRegistry.addShapelessRecipe(
                ItemAndBlockHolder.WIRELESS_DISTRIBUTOR.stack(),
                ItemAndBlockHolder.WIRELESS_DISTRIBUTOR.stack(i));
        }
        if (AE2_WIRELESS != null && AE2_CERTUS != null) {
            GameRegistry.addShapelessRecipe(ItemAndBlockHolder.WIRE_CUTTER.stack(), AE2_WIRELESS, AE2_CERTUS);
        }
        if (Mods.THAUMCRAFT.isModLoaded() && Mods.THAUMIC_EXPLORATION.isModLoaded()
            && Mods.CORE_MOD.isModLoaded()
            && AE2_SINGULARITY != null
            && EVERBURN_URN != null) {
            GTRecipeBuilder.builder()
                .circuit(20)
                .itemInputs(CELL_HOUSING.stack(1, 1), AE2_SINGULARITY, EVERBURN_URN)
                .itemOutputs(ItemAndBlockHolder.ITEM_CREATIVE_LAVA_CELL.stack(1))
                .duration(400)
                .eut(TierEU.RECIPE_LuV)
                .addTo(RecipeMaps.assemblerRecipes);
        }
    }
}
