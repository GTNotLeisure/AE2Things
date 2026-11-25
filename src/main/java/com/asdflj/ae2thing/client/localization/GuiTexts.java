package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum GuiTexts implements Localization {

    cell_link,
    pattern_modifier,
    infusion_interface,
    part_thaumatorium_interface,
    fluid_packet_encoder,
    pattern_rename,

    name("wireless_connector_terminal"),
    pos("wireless_connector_terminal"),
    color("wireless_connector_terminal"),
    channels_used("wireless_connector_terminal"),

    page("terminal_menu"),
    max_page("terminal_menu");

    private final String key;

    GuiTexts() {
        this.key = name();
    }

    GuiTexts(String prefix) {
        this.key = prefix + "." + name();
    }

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".gui." + this.key;
    }
}
