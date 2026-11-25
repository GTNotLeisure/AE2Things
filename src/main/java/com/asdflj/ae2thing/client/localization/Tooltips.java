package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum Tooltips implements Localization {

    cell_contents,
    cell_empty,
    ctrl_for_more,
    deprecation,
    for_full_contents,
    just_material,
    shift_for_more,
    linked,
    installed_card,

    backpack_terminal_desc("backpack_terminal", "desc"),
    toggleable_view_cell_desc("toggleable_view_cell", "desc"),
    wireless_distributor_desc("wireless_distributor", "desc"),
    wireless_dual_interface_terminal_desc("wireless_dual_interface_terminal", "desc"),

    cell_link_desc("cell_link", "desc"),
    cell_link_disabled("cell_link", "disabled"),

    fluid_packet_encoder_desc("fluid_packet_encoder", "desc"),

    crafting_debug_card_desc("crafting_debug_card", "desc"),
    crafting_debug_card_mode("crafting_debug_card", "current_mode"),

    pattern_modifier_desc("pattern_modifier", "desc"),
    pattern_modifier_mode("pattern_modifier", "current_mode"),
    pattern_modifier_insert("pattern_modifier.mode", "insert"),
    pattern_modifier_swap("pattern_modifier.mode", "swap"),

    wireless_installed("wireless", "installed"),

    universal_cell_stored("universal_cell", "stored"),
    universal_cell_empty("universal_cell", "empty"),

    magnet_off("magnet", "off"),
    magnet_inv("magnet", "inv"),
    magnet_backpack("magnet", "backpack"),
    magnet_current_mode("magnet", "current_mode");

    private final String key;

    Tooltips() {
        this.key = name();
    }

    Tooltips(String prefix, String key) {
        this.key = prefix.isEmpty() ? key : prefix + "." + key;
    }

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".tooltips." + this.key;
    }
}
