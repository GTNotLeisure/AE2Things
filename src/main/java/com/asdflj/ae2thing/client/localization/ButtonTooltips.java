package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum ButtonTooltips implements Localization {

    display("pin"),
    mode_auto("pin"),
    mode_fifo("pin"),
    mode_lru("pin"),
    crafting_rows("pin"),
    fixed("pin"),
    hold_alt("pin"),
    inventory("pin"),
    remove("pin"),
    rows("pin"),
    set_body("pin"),
    set_content("pin"),
    scrolling("pin"),
    toggle("pin"),

    store_action("terminal"),
    left_click("terminal"),
    right_click("terminal"),

    replace("pattern_modifier"),
    clear("pattern_modifier"),

    bind("wireless_connector_terminal"),
    unbind("wireless_connector_terminal"),

    crafting_pattern("infusion_terminal"),
    processing_pattern("infusion_terminal"),

    replan("button"),

    encode_auto_upload_title("encode_auto_upload", "title"),
    encode_auto_upload_desc("encode_auto_upload", "desc_short");

    private final String key;

    ButtonTooltips(String prefix) {
        this.key = prefix + "." + name();
    }

    ButtonTooltips(String prefix, String key) {
        this.key = prefix.isEmpty() ? key : prefix + "." + key;
    }

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".gui." + this.key;
    }
}
