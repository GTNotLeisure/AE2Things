package com.asdflj.ae2thing.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.input.Mouse;

public record MousePos(int x, int y) {

    public MousePos() {
        Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        this(Mouse.getX() * i / mc.displayWidth, j - Mouse.getY() * j / mc.displayHeight - 1);
    }
}
