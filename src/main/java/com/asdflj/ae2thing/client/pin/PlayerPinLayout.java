package com.asdflj.ae2thing.client.pin;

import appeng.items.contents.PinList;

public record PlayerPinLayout(int craftingRows, int playerRows, int listRows, int capacity, int width,
    boolean scrolling) {

    public PlayerPinLayout(int rows, int width, int crafting, int player, boolean scrolling) {
        rows = Math.max(1, rows);
        width = Math.max(1, width);
        crafting = Math.clamp(crafting, 0, 16);
        player = Math.clamp(player, 0, 16);
        int visiblePlayer = scrolling ? 0 : Math.min(player, rows - 1);
        int visibleCraft = Math.min(crafting, rows - 1 - visiblePlayer);
        this(visibleCraft, visiblePlayer, rows - visiblePlayer - visibleCraft, player * 9, width, scrolling);
    }

    public int prefixSize() {
        return scrolling ? (capacity + width - 1) / width * width : 0;
    }

    public int absoluteIndex(int screenIndex, int scrollRow) {
        return screenIndex + scrollRow * width;
    }

    public int playerIndex(int screenIndex, int scrollRow) {
        int absolute = absoluteIndex(screenIndex, scrollRow);
        return scrolling && absolute >= 0 && absolute < capacity ? PinList.PLAYER_OFFSET + absolute : -1;
    }

    public boolean isPadding(int screenIndex, int scrollRow) {
        int absolute = absoluteIndex(screenIndex, scrollRow);
        return scrolling && absolute >= capacity && absolute < prefixSize();
    }

    public int scrollMax(int contentSize) {
        return Math.max(0, (contentSize + width - 1) / width - listRows);
    }
}
