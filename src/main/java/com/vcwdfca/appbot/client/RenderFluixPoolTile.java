package com.vcwdfca.appbot.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.vcwdfca.appbot.tile.TileFluixPool;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MultiblockRenderHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.model.ModelPool;
import vazkii.botania.common.block.mana.BlockPool;
import vazkii.botania.common.block.tile.mana.TilePool;

@SideOnly(Side.CLIENT)
public class RenderFluixPoolTile extends TileEntitySpecialRenderer {

    private static final ResourceLocation FALLBACK = new ResourceLocation("botania", "textures/model/pool.png");
    private static final ModelPool MODEL = new ModelPool();

    public static boolean forceFull = false;
    public static int forceManaNumber = -1;

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        final TilePool pool = (TilePool) tileEntity;
        final Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        final float a = MultiblockRenderHandler.rendering ? 0.6F : 1F;
        GL11.glColor4f(1F, 1F, 1F, a);
        GL11.glTranslated(x, y, z);
        if (!FluixPoolModelSupport.isJsonModels()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5F, 1.5F, 0.5F);
            GL11.glScalef(1F, -1F, -1F);
            mc.renderEngine.bindTexture(FALLBACK);
            MODEL.render();
            GL11.glPopMatrix();
        }
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glTranslatef(0.5F, 1.5F, 0.5F);

        int mana = pool.getCurrentMana();
        if (forceManaNumber > -1) mana = forceManaNumber;

        final int networkCap = pool instanceof TileFluixPool fluidPool ? fluidPool.getManaCapacity() : 0;
        final int cap = networkCap > 0 ? networkCap : (pool.manaCap > 0 ? pool.manaCap : TilePool.MAX_MANA);
        float waterLevel = mana <= 0 ? 0F : Math.min(1F, (float) mana / (float) cap) * 0.4F;
        if (forceFull) waterLevel = 0.4F;

        final float s = 1F / 16F;
        final float v = 1F / 8F;
        final float w = -v * 3.5F;

        if (pool.getWorldObj() != null) {
            final Block below = pool.getWorldObj()
                .getBlock(pool.xCoord, pool.yCoord - 1, pool.zCoord);
            if (below instanceof IPoolOverlayProvider provider) {
                final IIcon overlay = provider.getIcon(pool.getWorldObj(), pool.xCoord, pool.yCoord - 1, pool.zCoord);
                if (overlay != null) {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    GL11.glColor4f(
                        1F,
                        1F,
                        1F,
                        a * (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 20.0) + 1) * 0.3
                            + 0.2));
                    GL11.glTranslatef(-0.5F, -1F - 0.43F, -0.5F);
                    GL11.glRotatef(90F, 1F, 0F, 0F);
                    GL11.glScalef(s, s, s);
                    RenderHelper.renderIcon(0, 0, overlay, 16, 16, 240);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }
        }

        if (waterLevel > 0 && BlockPool.manaIcon != null) {
            final float iconScale = 1F / 256F * 14F;
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glColor4f(1F, 1F, 1F, a);
            GL11.glTranslatef(w, -1F - (0.43F - waterLevel), w);
            GL11.glRotatef(90F, 1F, 0F, 0F);
            GL11.glScalef(iconScale, iconScale, iconScale);
            ShaderHelper.useShader(ShaderHelper.manaPool);
            RenderHelper.renderIcon(0, 0, BlockPool.manaIcon, 16, 16, 240);
            ShaderHelper.releaseShader();
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        forceFull = false;
        forceManaNumber = -1;
    }
}
