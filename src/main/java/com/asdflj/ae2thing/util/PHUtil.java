package com.asdflj.ae2thing.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.nei.object.OrderStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;

public class PHUtil {

    public static ObjectList<OrderStack<?>> transfer(ObjectList<OrderStack<?>> inputs) {
        final AtomicBoolean circuit = new AtomicBoolean(false);
        if (!ItemProgrammingToolkit.holding()) {
            return inputs;
        }
        final AtomicInteger i = new AtomicInteger(0);
        final ObjectList<OrderStack<?>> spec = new ObjectArrayList<>();
        final ObjectList<OrderStack<?>> filtered = new ObjectArrayList<>();
        inputs.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(OrderStack::getIndex))
            .forEach(orderStack -> {
                if (orderStack.getStack() instanceof ItemStack is && is.stackSize == 0) {
                    circuit.set(true);
                    spec.add(new OrderStack<>(ItemProgrammingCircuit.wrap(is), orderStack.getIndex()));
                } else {
                    filtered.add(orderStack);
                }
            });

        if (!circuit.get() && ItemProgrammingToolkit.addEmptyProgCiruit()) {
            spec.addFirst(new OrderStack<>(ItemProgrammingCircuit.wrap(null), 0));
        }

        spec.addAll(filtered);
        spec.forEach(orderStack -> orderStack.setIndex(i.getAndIncrement()));

        return spec;
    }

    public static boolean isProgrammingToolkitEnabled() {
        return ItemProgrammingToolkit.holding() && ItemProgrammingToolkit.mode != 0;
    }
}
