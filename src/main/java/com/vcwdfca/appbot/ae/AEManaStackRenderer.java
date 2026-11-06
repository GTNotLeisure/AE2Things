package com.vcwdfca.appbot.ae;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import com.vcwdfca.appbot.client.ManaIcons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AEManaStackRenderer {

    private AEManaStackRenderer() {}

    public static void drawInGui(int x, int y) {
        final IIcon icon = ManaIcons.MANA;
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(ManaIcons.TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + 16, 0D, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + 16, y + 16, 0D, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + 16, y, 0D, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(x, y, 0D, icon.getMinU(), icon.getMinV());
        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }
}
