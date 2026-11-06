package com.vcwdfca.appbot.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.util.NameConst;
import com.vcwdfca.appbot.part.PartManaImportBus;
import com.vcwdfca.appbot.part.SharedManaBus;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;

public class GuiManaIO extends GuiUpgradeable {

    private final SharedManaBus bus;

    public GuiManaIO(InventoryPlayer inventoryPlayer, SharedManaBus te) {
        super(new ContainerManaIO(inventoryPlayer, te));
        this.bus = te;
    }

    @Override
    protected String getBackground() {
        return "guis/bus.png";
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.bindTexture(this.getBackground());
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.drawTexturedModalRect(offsetX + 61 + 18 * x, offsetY + 21 + 18 * y, 79, 21, 18, 18);
            }
        }
    }

    @Override
    protected String getName() {
        return (this.bus instanceof PartManaImportBus ? GuiText.ImportBus : GuiText.ExportBus).getLocal();
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
            this.getGuiDisplayName(
                I18n.format(
                    this.bus instanceof PartManaImportBus ? NameConst.GUI_MANA_IMPORT : NameConst.GUI_MANA_EXPORT)),
            8,
            6,
            4210752);
        this.fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }
    }

}
