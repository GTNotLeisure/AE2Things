package com.asdflj.ae2thing.integration;

import net.minecraft.item.ItemStack;

public enum Compat {

    ae2TypeFilter("appeng.core.features.registries.ItemDisplayRegistry"),
    gtAvailableRecipeMaps("gregtech.api.interfaces.tileentity.RecipeMapWorkable", "getAvailableRecipeMaps"),
    gtIntegratedCircuit("gregtech.api.util.GTUtility", "isAnyIntegratedCircuit", ItemStack.class),
    gtNonConsumedDisplay("gregtech.api.interfaces.INonConsumedItemDisplay"),
    gtRecipeMap("gregtech.api.recipe.RecipeMap");

    private final String className;
    private final String method;
    private final Class<?>[] parameterTypes;
    private Boolean value;

    Compat(String className) {
        this(className, null);
    }

    Compat(String className, String method, Class<?>... parameterTypes) {
        this.className = className;
        this.method = method;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
    }

    public boolean isPresent() {
        if (this.value == null) {
            try {
                final Class<?> target = Class.forName(this.className);
                if (this.method != null) {
                    target.getMethod(this.method, this.parameterTypes);
                }
                this.value = true;
            } catch (Throwable ignored) {
                this.value = false;
            }
        }
        return this.value;
    }

    public static boolean hasAe2TypeFilter() {
        return ae2TypeFilter.isPresent();
    }

    public static boolean hasGTNonConsumedDisplay() {
        return gtNonConsumedDisplay.isPresent();
    }

    public static boolean hasGTIntegratedCircuit() {
        return gtIntegratedCircuit.isPresent();
    }

    public static boolean hasNotGTRecipeMapWorkable() {
        return !gtAvailableRecipeMaps.isPresent();
    }

    public static boolean hasGTRecipeMap() {
        return gtRecipeMap.isPresent();
    }
}
