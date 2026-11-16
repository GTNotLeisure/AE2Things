package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternModifier;
import com.asdflj.ae2thing.client.gui.container.slot.SlotEncodedPatternInput;
import com.asdflj.ae2thing.client.gui.widget.PatternValueSlot;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.AEBaseGui;
import appeng.core.localization.ColorUtils;
import appeng.core.localization.GuiText;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import it.unimi.dsi.fastutil.objects.ObjectLists;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiPatternModifier extends AEBaseGui implements INEIGuiHandler {

    protected GuiButton replace;
    protected GuiButton clear;
    protected ContainerPatternModifier container;

    public GuiPatternModifier(InventoryPlayer inventory, ITerminalHost inv) {
        super(new ContainerPatternModifier(inventory, inv));
        this.container = (ContainerPatternModifier) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 207;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_PATTERN_MODIFIER)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        this.mc.getTextureManager()
            .bindTexture(loc);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.initReplaceSlots();
        this.buttonList.add(
            this.replace = new GuiButton(
                0,
                this.guiLeft + 70,
                this.guiTop + 92,
                50,
                20,
                I18n.format(NameConst.GUI_PATTERN_MODIFIER_REPLACE)));
        this.buttonList.add(
            this.clear = new GuiButton(
                0,
                this.guiLeft + 70 + 50 + 2,
                this.guiTop + 92,
                50,
                20,
                I18n.format(NameConst.GUI_PATTERN_MODIFIER_CLEAR)));
    }

    private void initReplaceSlots() {
        if (this.container.replaceSourceSync == null || this.container.replaceTargetSync == null) return;
        var replaceSourceSlot = new PatternValueSlot(
            8,
            93,
            this.container.replaceSourceSync,
            0,
            (_, _, _) -> true,
            true);
        var replaceTargetSlot = new PatternValueSlot(
            50,
            93,
            this.container.replaceTargetSync,
            0,
            (_, _, _) -> true,
            true);
        replaceSourceSlot.setShowAmount(false);
        replaceTargetSlot.setShowAmount(false);
        this.registerVirtualSlots(replaceSourceSlot);
        this.registerVirtualSlots(replaceTargetSlot);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
    }

    protected String getBackground() {
        return "gui/pattern_modifier.png";
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.replace) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketInventoryActionExtend(InventoryActionExtend.REPLACE_PATTERN));
        } else if (button == this.clear) {
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketInventoryActionExtend(InventoryActionExtend.CLEAR_PATTERN));
        }
        super.actionPerformed(button);
    }

    @Override
    public void func_146977_a(final Slot s) {
        drawSlot(s);
        super.func_146977_a(s);
    }

    private void drawSlot(final Slot s) {
        if (!(s instanceof final SlotEncodedPatternInput p) || !p.getHasStack()) return;
        final ItemStack stack = p.getStack();
        if (stack.getItem() == null || !(stack.getItem() instanceof final ICraftingPatternItem cr)) return;
        final IAEStack<?> aes = this.container.getReplaceAEStack(StorageName.CRAFTING_INPUT);
        if (aes == null) return;
        final ICraftingPatternDetails details = cr.getPatternForItem(stack, this.container.getInventoryPlayer().player.worldObj);
        if (details == null) return;
        if (drawSlotBG(details.getAEInputs(), aes)) return;
        if (drawSlotBG(details.getAEOutputs(), aes)) return;
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        GL11.glDisable(GL11.GL_LIGHTING);
        drawRect(
            s.xDisplayPosition,
            s.yDisplayPosition,
            16 + s.xDisplayPosition,
            16 + s.yDisplayPosition,
            ColorUtils.itemSlotOverlayUnpowered.getColor());
        GL11.glEnable(GL11.GL_LIGHTING);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    private boolean drawSlotBG(IAEStack<?>[] list, IAEStack<?> item) {
        for (IAEStack<?> is : list) {
            if (is == null) continue;
            if (is.isSameType(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return ObjectLists.emptyList();
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        if (draggedStack == null) {
            return false;
        }
        Slot slotAtPosition = this.getSlotAtPosition(mouseX, mouseY);
        if (slotAtPosition == null) {
            return false;
        }
        if (this.getVirtualMESlotUnderMouse() != null) {
            return super.handleDragNDrop(gui, mouseX, mouseY, draggedStack, button);
        }
        return false;
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton) {
        // maybe we don't need set value ?
        // if (mouseButton == 3 && slot instanceof SlotReplaceFake && slot.getHasStack()) {
        // IAEItemStack stack = AEItemStack.create(slot.getStack());
        // this.container.setTargetStack(stack);
        // for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
        // if (slot.equals(this.inventorySlots.inventorySlots.get(i))) {
        // InventoryAction action = InventoryAction.SET_PATTERN_VALUE;
        // AE2Thing.proxy.netHandler.sendToServer(new CPacketInventoryAction(action, i, 0, stack));
        // return;
        // }
        // }
        // }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }
}
