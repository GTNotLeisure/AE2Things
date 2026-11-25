package com.asdflj.ae2thing.client.localization;

import com.asdflj.ae2thing.AE2Thing;

import appeng.core.localization.Localization;

public enum Messages implements Localization {

    scanning,
    permission_view_denied("security", "view_denied"),

    crafting_request_type("crafting_debug_card", "request_type"),
    crafting_name("crafting_debug_card", "name"),
    crafting_state("crafting_debug_card", "state"),
    crafting_finish("crafting_debug_card", "finish"),
    crafting_cancel("crafting_debug_card", "cancel"),
    crafting_running("crafting_debug_card", "running"),
    crafting_error_message("crafting_debug_card", "error_message"),
    crafting_simulation("crafting_debug_card", "simulation"),
    crafting_start_time("crafting_debug_card", "start_time"),
    crafting_usage_time("crafting_debug_card", "usage_time"),
    crafting_network_id("crafting_debug_card", "network_id"),
    crafting_pos("crafting_debug_card", "pos"),
    crafting_stack("crafting_debug_card", "stack"),
    crafting_no_history("crafting_debug_card", "no_history"),
    crafting_request_machine("crafting_debug_card.request_type", "machine"),
    crafting_request_player("crafting_debug_card.request_type", "player"),
    crafting_request_everything("crafting_debug_card.request_type", "everything"),
    crafting_export_file("crafting_debug_card", "export_file"),

    pattern_modifier_no_match("pattern_modifier", "no_match"),
    pattern_modifier_skipped("pattern_modifier", "skipped"),

    auto_upload_no_blank_pattern("auto_upload", "no_blank_pattern"),
    auto_upload_slot_occupied("auto_upload", "slot_occupied"),
    auto_upload_duplicate("auto_upload", "duplicate"),
    auto_upload_failed("auto_upload", "failed"),

    universal_bus_config_full("universal_bus", "config_full"),
    pattern_capacity_exceeded("dual_interface_terminal", "pattern_capacity_exceeded"),
    data_stick_link_saved("data_stick", "link_saved"),

    replan_failed,
    crafting_tree_unavailable,

    find_cell_highlight("find_cell_item", "highlight"),
    find_cell_in_other_dim("find_cell_item", "in_other_dim"),

    wireless_connector_highlight("wireless_connector_terminal", "highlight"),
    wireless_connector_in_other_dim("wireless_connector_terminal", "in_other_dim");

    private final String key;

    Messages() {
        this.key = name();
    }

    Messages(String prefix, String key) {
        this.key = prefix.isEmpty() ? key : prefix + "." + key;
    }

    @Override
    public String getUnlocalized() {
        return AE2Thing.MODID + ".message." + this.key;
    }
}
