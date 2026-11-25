package com.vcwdfca.appbot.client.localization;

import com.vcwdfca.appbot.AppliedBotanics;

import appeng.core.localization.Localization;

public enum Tooltips implements Localization {

    cell_creative("mana"),
    cell_idle("mana"),
    cell_stored("mana"),
    name("mana"),
    stored("mana"),
    marker("mana"),
    unit("mana");

    private final String key;

    Tooltips(String prefix) {
        this.key = prefix + "." + name();
    }

    @Override
    public String getUnlocalized() {
        return AppliedBotanics.ID + ".tooltips." + this.key;
    }
}
