package com.asdflj.ae2thing.nei.object;

import java.util.List;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface IRecipeExtractor {

    ObjectList<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs);

    ObjectList<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs);

    default ObjectList<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe,
        int index) {
        return getInputIngredients(rawInputs);
    }

    default ObjectList<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
        int index) {
        return getOutputIngredients(rawOutputs);
    }
}
