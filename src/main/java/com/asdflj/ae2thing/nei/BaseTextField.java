package com.asdflj.ae2thing.nei;

import codechicken.lib.config.ConfigTag;
import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionTextField;

public class BaseTextField extends OptionTextField {

    private static final ConfigTagParent tag = NEIClientConfig.global.config;

    public BaseTextField(String name) {
        this(name, "");
    }

    public BaseTextField(String name, String defaultValue) {
        super(name);
        tag.getTag(name)
            .setDefaultValue(defaultValue);
    }

    @Override
    public void update() {
        if (getSlot() == null) return;
        final ConfigTag optionTag = renderTag();
        if (optionTag.getValue() == null) {
            optionTag.setValue("");
        }
        super.update();
    }
}
