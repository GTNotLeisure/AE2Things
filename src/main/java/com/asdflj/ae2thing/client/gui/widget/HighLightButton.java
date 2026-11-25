package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;

import com.asdflj.ae2thing.client.localization.Messages;
import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.util.Info;

import appeng.core.localization.PlayerMessages;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public class HighLightButton extends BaseGuiButton {

    private final Minecraft mc = Minecraft.getMinecraft();

    public HighLightButton(int xPos, int yPos, int width, int height, int offsetX, int offsetY, Component component) {
        super(xPos, yPos, width, height, offsetX, offsetY, component);
        this.visible = true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

    }

    @Override
    public void onClick() {
        Info info = component.getInfo();
        if (info != null) {
            BlockPosHighlighter.highlightBlocks(
                mc.thePlayer,
                ObjectLists.singleton(info.a),
                PlayerMessages.InterfaceHighlighted,
                Messages.wireless_connector_in_other_dim);
            mc.thePlayer.closeScreen();
        }
    }

    @Override
    public int getIndex() {
        return 1;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
