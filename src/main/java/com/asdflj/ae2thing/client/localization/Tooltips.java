package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum Tooltips implements Localization {

    just_material;

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".tooltips." + this;
    }
}
