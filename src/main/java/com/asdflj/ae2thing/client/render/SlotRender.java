package com.asdflj.ae2thing.client.render;

import java.util.Collection;

import com.asdflj.ae2thing.integration.Mods;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class SlotRender {

    private static final Object2ObjectOpenHashMap<Class<? extends ISlotRender>, ISlotRender> renders = new Object2ObjectOpenHashMap<>();
    private static final SlotRender API = new SlotRender();

    private SlotRender() {
        registerSlotRenderHandler(RenderFluidStack.class, new RenderFluidStack());
        registerSlotRenderHandler(RenderEncodedPattern.class, new RenderEncodedPattern());
        registerSlotRenderHandler(RenderPatternSlotFake.class, new RenderPatternSlotFake());
        if (Mods.THAUMIC_ENERGISTICS.isModLoaded()) {
            registerSlotRenderHandler(RenderItemPhial.class, new RenderItemPhial());
        }
    }

    public static SlotRender instance() {
        return API;
    }

    public void registerSlotRenderHandler(Class<? extends ISlotRender> cls, ISlotRender render) {
        renders.putIfAbsent(cls, render);
    }

    public Collection<ISlotRender> getRenders() {
        return renders.values();
    }

}
