package com.vcwdfca.appbot.client.localization;

import com.vcwdfca.appbot.AppliedBotanics;

import appeng.core.localization.Localization;

public enum GuiTexts implements Localization {

    part_mana_import,
    part_mana_export;

    private final String key;

    GuiTexts() {
        this.key = name();
    }

    @Override
    public String getUnlocalized() {
        return AppliedBotanics.ID + ".gui." + this.key;
    }
}
