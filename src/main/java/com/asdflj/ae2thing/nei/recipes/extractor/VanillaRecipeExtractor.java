package com.asdflj.ae2thing.nei.recipes.extractor;

import java.util.List;

import com.asdflj.ae2thing.nei.NEEUtils;
import com.asdflj.ae2thing.nei.NEIStackSelector;
import com.asdflj.ae2thing.nei.object.IRecipeExtractor;
import com.asdflj.ae2thing.nei.object.OrderStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class VanillaRecipeExtractor implements IRecipeExtractor {

    private final boolean c;

    public VanillaRecipeExtractor(boolean isCraft) {
        c = isCraft;
    }

    @Override
    public ObjectList<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        return convert(rawInputs, NEEUtils.NULL_IDENTIFIER, false);
    }

    @Override
    public ObjectList<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return convert(rawOutputs, NEEUtils.NULL_IDENTIFIER, true);
    }

    @Override
    public ObjectList<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe,
        int index) {
        return convert(rawInputs, NEEUtils.realIdentifierOf(recipe), false);
    }

    @Override
    public ObjectList<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
        int index) {
        return convert(rawOutputs, NEEUtils.realIdentifierOf(recipe), true);
    }

    private ObjectList<OrderStack<?>> convert(List<PositionedStack> raw, String identifier, boolean output) {
        final ObjectList<OrderStack<?>> tmp = new ObjectArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final PositionedStack positionedStack = raw.get(i);
            if (positionedStack == null) continue;
            final int index;
            if (c) {
                final int col = (positionedStack.relx - 25) / 18;
                final int row = (positionedStack.rely - 6) / 18;
                index = col + row * 3;
            } else {
                index = i;
            }
            final OrderStack<?> stack = NEIStackSelector.pack(positionedStack, index, identifier, output);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }
}
