package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum SearchTooltips implements Localization {

    hits,
    empty,
    current,
    pool,
    mode_mismatch,
    disabled,

    category_only("invalid"),
    category_hint("invalid"),

    wireless_connector;

    private final String key;

    SearchTooltips() {
        this.key = name();
    }

    SearchTooltips(String prefix) {
        this.key = prefix + "." + name();
    }

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".tooltips.search." + this.key;
    }
}
