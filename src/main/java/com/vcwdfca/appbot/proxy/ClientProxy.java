package com.vcwdfca.appbot.proxy;

import com.vcwdfca.appbot.client.FluixPoolModelSupport;
import com.vcwdfca.appbot.client.RenderFluixPool;
import com.vcwdfca.appbot.client.RenderFluixPoolTile;
import com.vcwdfca.appbot.tile.TileFluixPool;
import com.vcwdfca.appbot.util.FluixPoolRenderId;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        FluixPoolModelSupport.init();
    }

    @Override
    public void init() {
        super.init();
        FluixPoolRenderId.id = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new RenderFluixPool());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFluixPool.class, new RenderFluixPoolTile());
    }
}
