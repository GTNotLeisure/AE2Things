package com.asdflj.ae2thing.inventory.gui;

import net.minecraft.entity.player.EntityPlayer;

import com.vcwdfca.appbot.client.ContainerManaIO;
import com.vcwdfca.appbot.client.GuiManaIO;
import com.vcwdfca.appbot.part.SharedManaBus;

final class ManaBusGuiFactory extends PartGuiFactory<SharedManaBus> {

    ManaBusGuiFactory() {
        super(SharedManaBus.class);
    }

    @Override
    protected Object createServerGui(EntityPlayer player, SharedManaBus inv) {
        return new ContainerManaIO(player.inventory, inv);
    }

    @Override
    protected Object createClientGui(EntityPlayer player, SharedManaBus inv) {
        return new GuiManaIO(player.inventory, inv);
    }
}
