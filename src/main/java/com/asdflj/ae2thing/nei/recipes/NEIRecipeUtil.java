package com.asdflj.ae2thing.nei.recipes;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.nei.NEEUtils;
import com.asdflj.ae2thing.nei.NEIStackSelector;
import com.asdflj.ae2thing.nei.object.IRecipeExtractor;
import com.asdflj.ae2thing.nei.object.IRecipeExtractorLegacy;
import com.asdflj.ae2thing.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class NEIRecipeUtil {

    public static final ObjectSet<String> notOtherSet = new ObjectOpenHashSet<>();
    public static final ObjectSet<String> craftSet = new ObjectOpenHashSet<>();
    public static final ObjectSet<String> noMergeSet = new ObjectOpenHashSet<>();

    private static final Object2ObjectOpenHashMap<String, IRecipeExtractor> IdentifierMap = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, IRecipeExtractor> IdentifierMapLegacy = new Object2ObjectOpenHashMap<>();

    static {
        notOtherSet.add("smelting");
        notOtherSet.add("brewing");
        craftSet.add("crafting");
        craftSet.add("crafting2x2");
        noMergeSet.add("gt.recipe.fakeAssemblylineProcess");
    }

    public static void addRecipeMap(String recipeIdentifier, IRecipeExtractor extractor) {
        IdentifierMap.put(recipeIdentifier, extractor);
        if (recipeIdentifier == null && extractor instanceof IRecipeExtractorLegacy legacy) {
            IdentifierMapLegacy.put(legacy.getClassName(), extractor);
        }
    }

    public static ObjectList<OrderStack<?>> getPackageInputs(IRecipeHandler recipe, int index, boolean priority) {
        final ObjectList<OrderStack<?>> out = collectInputs(recipe, index);
        return priority ? prioritizeFluids(out) : out;
    }

    public static ObjectList<OrderStack<?>> getPackageOutputs(IRecipeHandler recipe, int index, boolean useOther) {
        return collectOutputs(recipe, index, useOther);
    }

    public static ObjectList<String> getSupportRecipes() {
        return new ObjectArrayList<>(IdentifierMap.keySet());
    }

    private static ObjectList<OrderStack<?>> collectInputs(IRecipeHandler recipe, int index) {
        if (!(recipe instanceof TemplateRecipeHandler tRecipe)) return ObjectLists.emptyList();

        final String identifier = tRecipe.getOverlayIdentifier();
        if (!IdentifierMap.containsKey(identifier)) {
            final ObjectList<OrderStack<?>> NEEInputs = collectNEEInputs(tRecipe, index, identifier);
            if (NEEInputs != null) return NEEInputs;
            return getDefaultPackageInputs(tRecipe, index);
        }
        if (identifier == null) return getPackageInputsLegacy(recipe, index);
        final IRecipeExtractor extractor = IdentifierMap.get(identifier);
        if (extractor == null) return ObjectLists.emptyList();
        return extractor.getInputIngredients(new ObjectArrayList<>(tRecipe.getIngredientStacks(index)), recipe, index);
    }

    private static ObjectList<OrderStack<?>> collectOutputs(IRecipeHandler recipe, int index, boolean useOther) {
        if (!(recipe instanceof TemplateRecipeHandler tRecipe)) return ObjectLists.emptyList();

        final String identifier = tRecipe.getOverlayIdentifier();
        if (!IdentifierMap.containsKey(identifier)) {
            final ObjectList<OrderStack<?>> NEEOutputs = collectNEEOutputs(tRecipe, index, identifier);
            if (NEEOutputs != null) return NEEOutputs;
            return getDefaultPackageOutputs(tRecipe, index);
        }
        if (identifier == null) return getPackageOutputsLegacy(recipe, index, useOther);
        final IRecipeExtractor extractor = IdentifierMap.get(identifier);
        if (extractor == null) return ObjectLists.emptyList();
        final ObjectList<PositionedStack> tmp = new ObjectArrayList<>();
        final PositionedStack result = tRecipe.getResultStack(index);
        if (result != null) tmp.add(result);
        if (useOther) tmp.addAll(tRecipe.getOtherStacks(index));
        return extractor.getOutputIngredients(tmp, recipe, index);
    }

    private static ObjectList<OrderStack<?>> collectNEEInputs(TemplateRecipeHandler recipe, int index,
        String identifier) {
        if (identifier == null || !Mods.NOT_ENOUGH_ENERGISTICS.isModLoaded()) return null;
        final NEEUtils.Extraction extraction = NEEUtils.extract(recipe, index, identifier);
        if (extraction.unMatched()) return null;
        return pack(extraction.inputs(), identifier, false);
    }

    private static ObjectList<OrderStack<?>> collectNEEOutputs(TemplateRecipeHandler recipe, int index,
        String identifier) {
        if (identifier == null || !Mods.NOT_ENOUGH_ENERGISTICS.isModLoaded()) return null;
        final NEEUtils.Extraction extraction = NEEUtils.extract(recipe, index, identifier);
        if (extraction.unMatched()) return null;
        return pack(extraction.outputs(), identifier, true);
    }

    private static ObjectList<OrderStack<?>> getDefaultPackageInputs(TemplateRecipeHandler tRecipe, int index) {
        try {
            return pack(tRecipe.getIngredientStacks(index), NEEUtils.realIdentifierOf(tRecipe), false);
        } catch (Exception e) {
            return ObjectLists.emptyList();
        }
    }

    private static ObjectList<OrderStack<?>> getDefaultPackageOutputs(TemplateRecipeHandler tRecipe, int index) {
        try {
            final ObjectList<PositionedStack> stacks = new ObjectArrayList<>();
            final PositionedStack result = tRecipe.getResultStack(index);
            if (result != null) stacks.add(result);
            stacks.addAll(tRecipe.getOtherStacks(index));
            return pack(stacks, NEEUtils.realIdentifierOf(tRecipe), true);
        } catch (Exception e) {
            return ObjectLists.emptyList();
        }
    }

    private static ObjectList<OrderStack<?>> pack(List<PositionedStack> stacks, String identifier, boolean output) {
        final ObjectList<OrderStack<?>> out = new ObjectArrayList<>(stacks.size());
        int index = 0;
        for (PositionedStack ps : stacks) {
            if (ps == null) continue;
            final OrderStack<?> orderStack = NEIStackSelector.pack(ps, index, identifier, output);
            if (orderStack == null) continue;
            out.add(orderStack);
            index++;
        }
        return out;
    }

    private static ObjectList<OrderStack<?>> prioritizeFluids(ObjectList<OrderStack<?>> out) {
        final ObjectList<OrderStack<?>> reordered = new ObjectArrayList<>(out.size());
        int index = 0;
        for (OrderStack<?> orderStack : out) {
            if (orderStack != null && orderStack.getStack() instanceof FluidStack) {
                reordered.add(new OrderStack<>(orderStack.getStack(), index++));
            }
        }
        for (OrderStack<?> orderStack : out) {
            if (orderStack != null && orderStack.getStack() instanceof ItemStack) {
                reordered.add(new OrderStack<>(orderStack.getStack(), index++));
            }
        }
        for (OrderStack<?> orderStack : out) {
            if (orderStack != null && !(orderStack.getStack() instanceof FluidStack)
                && !(orderStack.getStack() instanceof ItemStack)) {
                reordered.add(new OrderStack<>(orderStack.getStack(), index++));
            }
        }
        return reordered;
    }

    public static ObjectList<OrderStack<?>> getPackageInputsLegacy(IRecipeHandler recipe, int index) {
        final IRecipeExtractor extractor = legacyExtractor(recipe);
        if (extractor == null) return ObjectLists.emptyList();
        return extractor.getInputIngredients(new ObjectArrayList<>(recipe.getIngredientStacks(index)), recipe, index);
    }

    public static ObjectList<OrderStack<?>> getPackageOutputsLegacy(IRecipeHandler recipe, int index,
        boolean useOther) {
        final IRecipeExtractor extractor = legacyExtractor(recipe);
        if (extractor == null) return ObjectLists.emptyList();
        final ObjectList<PositionedStack> tmp = new ObjectArrayList<>();
        final PositionedStack result = recipe.getResultStack(index);
        if (result != null) tmp.add(result);
        if (useOther) tmp.addAll(recipe.getOtherStacks(index));
        return extractor.getOutputIngredients(tmp, recipe, index);
    }

    private static IRecipeExtractor legacyExtractor(IRecipeHandler recipe) {
        if (recipe == null) return null;
        return IdentifierMapLegacy.get(
            recipe.getClass()
                .getName());
    }

    public static boolean notUseOther(IRecipeHandler recipeHandler) {
        if (!(recipeHandler instanceof TemplateRecipeHandler template)) {
            return true;
        }

        return notOtherSet.contains(template.getOverlayIdentifier());
    }

    public static boolean shouldCraft(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return craftSet.contains(tRecipe.getOverlayIdentifier());
    }
}
