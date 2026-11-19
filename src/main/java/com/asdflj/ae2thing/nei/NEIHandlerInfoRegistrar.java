package com.asdflj.ae2thing.nei;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;

import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class NEIHandlerInfoRegistrar {

    public static final NEIHandlerInfoRegistrar INSTANCE = new NEIHandlerInfoRegistrar();

    @SubscribeEvent
    public void onRegisterHandlerInfos(final NEIRegisterHandlerInfosEvent event) {
        event.registerHandlerInfo(
            InfinityStorageCellUsageHandler.class,
            AE2Thing.NAME,
            AE2Thing.MODID,
            builder -> builder.setDisplayStack(ItemAndBlockHolder.ITEM_INFINITY_UNIVERSAL_CELL.stack())
                .setShowFavoritesButton(false)
                .setShowOverlayButton(false)
                .setMultipleWidgetsAllowed(false)
                .setAllowOverflowY(true)
                .setShiftY(0)
                .setHeight(InfinityStorageCellUsageHandler.HANDLER_HEIGHT));
    }
}
