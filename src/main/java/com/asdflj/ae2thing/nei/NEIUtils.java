package com.asdflj.ae2thing.nei;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.NEIRecipeUtil;
import com.asdflj.ae2thing.util.GTUtil;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import gregtech.api.util.GTOreDictUnificator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class NEIUtils {

    public static ObjectList<OrderStack<?>> compress(ObjectList<OrderStack<?>> list) {
        final ObjectList<OrderStack<?>> comp = new ObjectArrayList<>();
        for (OrderStack<?> orderStack : list) {
            if (orderStack == null) continue;
            if (orderStack.getStack() instanceof FluidStack) {
                comp.add(orderStack);
                continue;
            }
            final ItemStack currentStack = (ItemStack) orderStack.getStack();
            if (currentStack.stackSize == 0) {
                comp.add(orderStack);
                continue;
            }
            boolean find = false;
            for (OrderStack<?> storedStack : comp) {
                if (storedStack == null || !(storedStack.getStack() instanceof ItemStack firstStack)) continue;
                boolean areItemStackEqual = firstStack.isItemEqual(currentStack)
                    && ItemStack.areItemStackTagsEqual(firstStack, currentStack);
                if (areItemStackEqual
                    && (firstStack.stackSize + currentStack.stackSize) <= firstStack.getMaxStackSize()) {
                    find = true;
                    ((ItemStack) storedStack.getStack()).stackSize = firstStack.stackSize + currentStack.stackSize;
                }
            }
            if (!find) {
                comp.add(orderStack);
            }
        }
        return comp;
    }

    public static ObjectList<OrderStack<?>> clearNull(ObjectList<OrderStack<?>> list) {
        final ObjectList<OrderStack<?>> out = new ObjectArrayList<>(list.size());
        int index = 0;
        for (OrderStack<?> orderStack : list) {
            if (orderStack == null) continue;
            if (orderStack.getStack() instanceof ItemStack is && is.stackSize == 0) continue;
            orderStack.setIndex(index++);
            out.add(orderStack);
        }
        return out;
    }

    public static ObjectList<OrderStack<?>> replaceHotIngot(ObjectList<OrderStack<?>> outputs) {
        if (outputs == null || outputs.isEmpty()) return outputs;

        final ObjectList<OrderStack<?>> result = new ObjectArrayList<>(outputs.size());
        for (OrderStack<?> src : outputs) {
            if (src == null || !(src.getStack() instanceof ItemStack is)) {
                result.add(src);
                continue;
            }

            for (int id : OreDictionary.getOreIDs(is)) {
                final String oreName = OreDictionary.getOreName(id);
                if (oreName == null || !oreName.startsWith("ingotHot")) continue;

                final String coldName = "ingot" + oreName.substring("ingotHot".length());
                final List<ItemStack> candidates = OreDictionary.getOres(coldName);
                if (candidates == null || candidates.isEmpty()) continue;

                // noinspection SequencedCollectionMethodCanBeUsed
                final ItemStack cold = candidates.get(0);
                if (cold == null) continue;

                ItemStack out = cold.copy();
                try {
                    final ItemStack unified = GTOreDictUnificator.setStack(out);
                    if (unified != null) out = unified;
                } catch (Throwable ignored) {}

                out.stackSize = is.stackSize;
                if (is.hasTagCompound()) {
                    out.setTagCompound(
                        (NBTTagCompound) is.getTagCompound()
                            .copy());
                }
                is = out;
            }

            result.add(new OrderStack<>(is, src.getIndex()));
        }

        return result;
    }

    public static ObjectList<OrderStack<?>> multiply(ObjectList<OrderStack<?>> list, int multiplier) {
        if (list == null || list.isEmpty() || multiplier <= 1) return list;
        final ObjectList<OrderStack<?>> out = new ObjectArrayList<>(list.size());
        for (OrderStack<?> src : list) {
            if (src == null) continue;
            if (src.getStack() instanceof ItemStack is) {
                final ItemStack copy = is.copy();
                if (copy.stackSize > 0) {
                    final long scaled = (long) copy.stackSize * multiplier;
                    copy.stackSize = (int) Math.min(Integer.MAX_VALUE, scaled);
                }

                final OrderStack<?> copyOS = new OrderStack<>(copy, src.getIndex());
                if (src.getItems() != null && src.getItems().length > 1) {
                    copyOS.putItems(src.getItems());
                }
                out.add(copyOS);
            } else if (src.getStack() instanceof FluidStack fs) {
                final FluidStack copy = fs.copy();
                if (copy.amount > 0) {
                    final long scaled = (long) copy.amount * multiplier;
                    copy.amount = (int) Math.min(Integer.MAX_VALUE, scaled);
                }
                out.add(new OrderStack<>(copy, src.getIndex()));
            } else {
                out.add(src);
            }
        }

        return out;
    }

    public static ObjectList<OrderStack<?>> prepareInputs(ObjectList<OrderStack<?>> list, boolean includeNonConsumed,
        IRecipeHandler handler, int recipeIndex) {
        if (list == null || list.isEmpty()) return list;
        final boolean circuitToBoard = isCircuitToBoardRecipe(handler, recipeIndex);
        final ObjectList<OrderStack<?>> out = new ObjectArrayList<>(list.size());
        for (OrderStack<?> orderStack : list) {
            if (orderStack == null) continue;
            if (orderStack.getStack() instanceof ItemStack is && is.stackSize == 0) {
                if (!includeNonConsumed) continue;
                is.stackSize = 1;
            }
            if (circuitToBoard && orderStack.getStack() instanceof ItemStack board
                && GTUtil.isUniversalCircuitBoard(board)) {
                final ItemStack[] candidates = orderStack.getItems();
                if (candidates != null) {
                    for (ItemStack candidate : candidates) {
                        if (!GTUtil.isPlainCircuit(candidate)) continue;
                        final ItemStack copy = candidate.copy();
                        copy.stackSize = board.stackSize;
                        orderStack.putStack(copy);
                        break;
                    }
                }
            }
            out.add(orderStack);
        }
        return out;
    }

    public static boolean isCircuitToBoardRecipe(IRecipeHandler handler, int recipeIndex) {
        try {
            boolean boardOutput = false;
            for (OrderStack<?> output : NEIRecipeUtil
                .getPackageOutputs(handler, recipeIndex, !NEIRecipeUtil.notUseOther(handler))) {
                if (output != null && output.getStack() instanceof ItemStack item
                    && GTUtil.isUniversalCircuitBoard(item)) {
                    boardOutput = true;
                    break;
                }
            }
            if (!boardOutput) return false;
            for (PositionedStack ingredient : handler.getIngredientStacks(recipeIndex)) {
                if (ingredient == null || ingredient.items == null) continue;
                for (ItemStack candidate : ingredient.items) {
                    if (GTUtil.isPlainCircuit(candidate)) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
