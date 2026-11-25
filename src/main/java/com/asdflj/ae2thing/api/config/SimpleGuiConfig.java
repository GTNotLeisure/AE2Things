package com.asdflj.ae2thing.api.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SimpleGuiConfig extends GuiConfig {

    private final Class<?> configClass;

    public SimpleGuiConfig(GuiScreen parent, Class<?> configClass, String modId, String modName)
        throws ConfigException {
        super(
            parent,
            ConfigurationManager.getConfigElements(configClass),
            modId,
            false,
            false,
            modName + " Configuration");
        this.configClass = configClass;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Every button can write the config: Done re-applies changed entries, Reset/Undo touch all of them.
        ConfigurationManager.withDeferredSave(configClass, () -> super.actionPerformed(button));
    }
}
