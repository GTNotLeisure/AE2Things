package com.vcwdfca.appbot.client;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FluixPoolModelSupport {

    private static boolean jsonModels;

    private FluixPoolModelSupport() {}

    public static void init() {
        if (!Loader.isModLoaded("gtnhlib")) return;
        try {
            com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry.registerModid("ae2thing_ab");
            jsonModels = true;
        } catch (Throwable ignored) {
            jsonModels = false;
        }
    }

    public static boolean isJsonModels() {
        return jsonModels;
    }
}
