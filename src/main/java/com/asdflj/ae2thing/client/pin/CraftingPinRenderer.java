package com.asdflj.ae2thing.client.pin;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.pin.ICraftingPinState;

import appeng.client.gui.slots.VirtualMEPinSlot;

public final class CraftingPinRenderer {

    private CraftingPinRenderer() {}

    public static void draw(VirtualMEPinSlot[] slots, ICraftingPinState state) {
        if (slots == null) return;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            float time = (Minecraft.getSystemTime() % 2400L) / 2400.0f;
            for (VirtualMEPinSlot slot : slots) {
                if (!slot.isCraftingSlot() || slot.isHidden() || slot.getAEStack() == null) continue;
                float phase = state.ae2t$isCraftingPinPending(slot.getSlotIndex()) ? time : 0;
                for (int i = 0; i < 72; i++) {
                    int edge = i / 18;
                    int position = i % 18;
                    int x = slot.getX() - 1 + switch (edge) {
                        case 0 -> position;
                        case 1 -> 17;
                        case 2 -> 17 - position;
                        default -> 0;
                    };
                    int y = slot.getY() - 1 + switch (edge) {
                        case 0 -> 0;
                        case 1 -> position;
                        case 2 -> 17;
                        default -> 17 - position;
                    };
                    int rgb = Color.HSBtoRGB((phase + i / 72.0f) % 1, 0.95f, 1);
                    tessellator.setColorRGBA(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 220);
                    tessellator.addVertex(x, y + 1, 180);
                    tessellator.addVertex(x + 1, y + 1, 180);
                    tessellator.addVertex(x + 1, y, 180);
                    tessellator.addVertex(x, y, 180);
                }
            }
            tessellator.draw();
        } finally {
            GL11.glPopAttrib();
        }
    }
}
