package com.vcwdfca.appbot.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ManaIcons {

    public static final ResourceLocation TEXTURE = new ResourceLocation("ae2thing_ab", "textures/gui/mana.png");
    public static final IIcon MANA = new AnimatedIcon(32, 2);
    private static IIcon manasteel;

    private ManaIcons() {}

    public static IIcon manasteel() {
        if (manasteel == null) {
            final Item item = GameRegistry.findItem("Botania", "manaResource");
            if (item != null) {
                manasteel = new ItemStack(item, 1, 0).getIconIndex();
            }
        }
        return manasteel;
    }

    private record AnimatedIcon(int frames, int frameTime) implements IIcon {

        private float frame() {
            return (float) ((System.currentTimeMillis() / (50L * this.frameTime)) % this.frames);
        }

        private float minV() {
            return this.frame() / this.frames;
        }

        private float maxV() {
            return (this.frame() + 1F) / this.frames;
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public float getMinU() {
            return 0F;
        }

        @Override
        public float getMaxU() {
            return 1F;
        }

        @Override
        public float getInterpolatedU(double u) {
            return (float) (u / 16D);
        }

        @Override
        public float getMinV() {
            return this.minV();
        }

        @Override
        public float getMaxV() {
            return this.maxV();
        }

        @Override
        public float getInterpolatedV(double v) {
            return this.minV() + (float) (v / 16D) * (this.maxV() - this.minV());
        }

        @Override
        public String getIconName() {
            return "mana";
        }
    }
}
