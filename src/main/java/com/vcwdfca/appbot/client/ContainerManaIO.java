package com.vcwdfca.appbot.client;

import net.minecraft.entity.player.InventoryPlayer;

import com.vcwdfca.appbot.part.SharedManaBus;

public class ContainerManaIO extends ContainerManaConfigurable {

    public ContainerManaIO(InventoryPlayer ip, SharedManaBus te) {
        super(ip, te);
    }
}
