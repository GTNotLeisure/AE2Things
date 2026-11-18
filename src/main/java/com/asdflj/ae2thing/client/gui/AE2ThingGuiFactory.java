package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiScreen;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.config.ConfigException;
import com.asdflj.ae2thing.api.config.SimpleGuiConfig;
import com.asdflj.ae2thing.api.config.SimpleGuiFactory;
import com.asdflj.ae2thing.common.AE2ThingConfig;

@SuppressWarnings("unused")
public final class AE2ThingGuiFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return Screen.class;
    }

    public static final class Screen extends SimpleGuiConfig {

        public Screen(GuiScreen parent) throws ConfigException {
            super(parent, AE2ThingConfig.class, AE2Thing.MODID, AE2Thing.NAME);
        }
    }
}
