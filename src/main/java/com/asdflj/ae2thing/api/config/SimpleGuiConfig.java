package com.asdflj.ae2thing.api.config;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SimpleGuiConfig extends GuiConfig {

    public SimpleGuiConfig(GuiScreen parent, Class<?> configClass, String modId, String modName)
        throws ConfigException {
        super(
            parent,
            ConfigurationManager.getConfigElements(configClass),
            modId,
            false,
            false,
            modName + " Configuration");
    }
}
