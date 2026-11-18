package com.asdflj.ae2thing.api.config;

import java.util.Set;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface SimpleGuiFactory extends IModGuiFactory {

    @Override
    default void initialize(Minecraft minecraft) {}

    @Override
    default Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    default RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
