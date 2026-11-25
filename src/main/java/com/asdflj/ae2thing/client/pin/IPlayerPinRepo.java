package com.asdflj.ae2thing.client.pin;

public interface IPlayerPinRepo {

    void ae2t$setPinLayout(PlayerPinLayout layout);

    int ae2t$playerPinIndex(int screenIndex);

    boolean ae2t$isPinPadding(int screenIndex);
}
