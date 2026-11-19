package com.asdflj.ae2thing.nei;

import java.awt.Point;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

import com.asdflj.ae2thing.client.me.InfinityCellContentsCache;
import com.asdflj.ae2thing.common.item.BaseInfinityCellItem;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;
import com.asdflj.ae2thing.util.Util;

import appeng.api.config.TerminalFontSize;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.client.render.StackSizeRenderer;
import appeng.core.localization.ColorUtils;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiRecipeTabs;
import codechicken.nei.recipe.IUsageHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class InfinityStorageCellUsageHandler implements IUsageHandler {

    public static final int HANDLER_HEIGHT = 160;
    private static final ResourceLocation SLOT_TEXTURE_LOCATION = new ResourceLocation("nei", "textures/slot.png");
    private static final int OFFSET_X = 2;
    private static final int INFO_OFFSET_Y = 4;
    private static final int ITEMS_OFFSET_Y = INFO_OFFSET_Y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2 + 6;
    private static final int COLUMNS = 8;
    private static final int SLOT_HEIGHT = 18;
    private static final int BOTTOM_MARGIN = 6;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private static final int PAGE_SIZE = InfinityStorageCellSnapshot.PAGE_SIZE;

    private final ObjectList<ViewItemStack> entries = new ObjectArrayList<>();
    private final ObjectList<NEIScrollArea.CellStack> positioned = new ObjectArrayList<>();
    private final ObjectList<PositionedStack> otherStacks = new ObjectArrayList<>();
    private final Reference2ObjectMap<IAEStack<?>, NEIScrollArea.CellStack> positionedCache = new Reference2ObjectOpenHashMap<>();

    private NEIScrollArea scrollArea;
    private ItemStack cell;
    private long loadedVersion = -1;
    private int rows = 1;
    private long usedTypes;
    private long totalTypes;
    private long usedBytes;
    private long totalBytes;
    private int bytesPerType;
    private boolean unlimited;
    private int requestTicker;

    @Override
    public IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
        if (ingredients.length == 0 || !(ingredients[0] instanceof ItemStack cellItem)) return null;
        if (!(cellItem.getItem() instanceof BaseInfinityCellItem<?>storageCell)) {
            return null;
        }

        this.cell = cellItem;
        this.scrollArea = null;
        this.loadedVersion = -1;
        this.rows = 1;
        this.totalTypes = storageCell.getTotalTypes(cell);
        this.totalBytes = storageCell.getBytesLong(cell);
        this.bytesPerType = storageCell.getBytesPerType(cell);
        this.unlimited = this.totalTypes >= Integer.MAX_VALUE;

        this.refreshStats();
        this.rebuild();
        return this;
    }

    private void refreshStats() {
        if (this.cell == null) return;
        long usedTypes = 0;
        long usedBytes = 0;
        for (final IAEStackType<?> type : Util.getTypes()) {
            InfinityCellContentsCache.poll(this.cell, type, PAGE_SIZE, 500L);

            final long types;
            final long count;
            final InfinityCellContentsCache.CellStats live = InfinityCellContentsCache.stats(this.cell, type);
            if (live != null) {
                types = live.types();
                count = live.count();
            } else {
                final NBTTagCompound section = InfinityStorageCellSnapshot.section(this.cell, type);
                if (section == null) continue;
                types = section.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES);
                count = section.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT);
            }
            if (types <= 0 && count <= 0) continue;
            usedTypes += types;
            usedBytes += count / Math.max(1, type.getAmountPerByte()) + types * (long) this.bytesPerType;
        }
        if (this.totalBytes > 0) usedBytes = Math.min(usedBytes, this.totalBytes);
        this.usedTypes = usedTypes;
        this.usedBytes = usedBytes;
    }

    private void rebuild() {
        if (this.cell == null) return;
        final ObjectList<IAEStack<?>> contents = new ObjectArrayList<>();
        for (final IAEStackType<?> type : Util.getTypes()) {
            contents.addAll(InfinityCellContentsCache.contents(this.cell, type));
        }
        contents.sort(
            Comparator.<IAEStack<?>>comparingLong(IAEStack::getStackSize)
                .reversed());

        this.entries.clear();
        this.positioned.clear();
        this.otherStacks.clear();
        final Reference2ObjectMap<IAEStack<?>, NEIScrollArea.CellStack> reused = new Reference2ObjectOpenHashMap<>();
        for (final IAEStack<?> aes : contents) {
            NEIScrollArea.CellStack stack = this.positionedCache.get(aes);
            if (stack == null) {
                final ItemStack display = aes.getItemStackForNEI();
                final ItemStack single = aes.getItemStackForNEI(1);
                final boolean bakeCount = aes.isFluid() || aes.getStackSize() < Integer.MAX_VALUE;
                final ItemStack item = bakeCount ? display : single;
                if (item == null) continue;
                stack = new NEIScrollArea.CellStack(item.copy());
            }
            reused.put(aes, stack);
            this.entries.add(
                new ViewItemStack(
                    stack,
                    aes.isFluid() || aes.getStackSize() < Integer.MAX_VALUE ? 0 : aes.getStackSize(),
                    aes.getStackSize()));
            this.positioned.add(stack);
            this.otherStacks.add(stack);
        }
        this.positionedCache.clear();
        this.positionedCache.putAll(reused);
        this.rows = Math.clamp((this.entries.size() + COLUMNS - 1) / COLUMNS, 1, maxRows());
        this.ensureScrollArea();
        this.scrollArea.setStacks(this.positioned);
        this.loadedVersion = InfinityCellContentsCache.version();
    }

    private void requestMoreIfNeeded() {
        if (this.scrollArea == null || this.cell == null) return;
        if ((this.requestTicker++ & 3) != 0) return;
        if (this.scrollArea.getFirstRow() + this.scrollArea.getVisibleRows() < this.scrollArea.getTotalRows()) return;
        for (final IAEStackType<?> type : Util.getTypes()) {
            InfinityCellContentsCache.requestMore(this.cell, type);
        }
    }

    private void ensureScrollArea() {
        if (this.scrollArea != null && this.scrollArea.getVisibleRows() == this.rows) return;
        this.scrollArea = new NEIScrollArea(OFFSET_X, ITEMS_OFFSET_Y, COLUMNS, this.rows);
    }

    private static int viewportHeight() {
        final Minecraft mc = Minecraft.getMinecraft();
        final int screenHeight = mc.currentScreen == null ? 240 : mc.currentScreen.height;
        final int pageChrome = 32 + 4;
        final int ySize = Math.min(
            screenHeight - 44 - GuiRecipeTabs.getTabHeight(),
            NEIClientConfig.getIntSetting("inventory.guirecipe.maxHeight"));
        final int minimumHeight = ITEMS_OFFSET_Y + BOTTOM_MARGIN + SLOT_HEIGHT;
        return Math.clamp(ySize - pageChrome, minimumHeight, Math.max(minimumHeight, HANDLER_HEIGHT));
    }

    private static int maxRows() {
        return Math.max(1, (viewportHeight() - ITEMS_OFFSET_Y - BOTTOM_MARGIN) / SLOT_HEIGHT);
    }

    @Override
    public int getRecipeHeight(int recipe) {
        return ITEMS_OFFSET_Y + this.rows * SLOT_HEIGHT + BOTTOM_MARGIN;
    }

    @Override
    public String getRecipeName() {
        return GuiText.CellView.getLocal();
    }

    @Override
    public int numRecipes() {
        return 1;
    }

    @Override
    public void drawBackground(int recipe) {
        this.refreshStats();
        if (InfinityCellContentsCache.version() != this.loadedVersion) {
            this.rebuild();
        }
        this.ensureScrollArea();
        this.requestMoreIfNeeded();
        if (this.scrollArea != null && NEIClientUtils.getGuiContainer() instanceof GuiRecipe<?>gui
            && this.scrollArea.isDragging()) {
            final Point mouse = gui.getRecipeMousePosition(recipe);
            this.scrollArea.updateDrag(Mouse.isButtonDown(0), mouse.x, mouse.y);
        }

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(SLOT_TEXTURE_LOCATION);
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        final int slots = Math.clamp(this.entries.size(), 1, COLUMNS * this.rows);
        for (int i = 0; i < slots; i++) {
            final int line = i % COLUMNS;
            final int row = i / COLUMNS;
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * row, 0, 1, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * row, 0, 0, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * (row + 1), 0, 0, 1);
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * (row + 1), 0, 1, 1);
        }
        tessellator.draw();
    }

    @Override
    public void drawForeground(int recipe) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final NumberFormat fmt = NUMBER_FORMAT;
        final int color = ColorUtils.neiCellView.getColor();

        final String byteLine = this.unlimited
            ? Platform.formatByteDouble(this.usedBytes) + ' ' + GuiText.BytesUsed.getLocal()
            : Platform.formatByteDouble(this.usedBytes) + ' '
                + GuiText.Of.getLocal()
                + ' '
                + Platform.formatByteDouble(this.totalBytes)
                + ' '
                + GuiText.BytesUsed.getLocal();
        final String typeLine = this.unlimited ? fmt.format(this.usedTypes) + ' ' + GuiText.Types.getLocal()
            : fmt.format(this.usedTypes) + ' '
                + GuiText.Of.getLocal()
                + ' '
                + fmt.format(this.totalTypes)
                + ' '
                + GuiText.Types.getLocal();
        fontRenderer.drawString(byteLine, OFFSET_X, INFO_OFFSET_Y, color);
        fontRenderer.drawString(typeLine, OFFSET_X, INFO_OFFSET_Y + fontRenderer.FONT_HEIGHT + 2, color);

        if (this.scrollArea == null) return;
        final int from = this.scrollArea.visibleFrom();
        final int to = this.scrollArea.visibleTo(this.entries.size());
        for (int i = from; i < to; i++) {
            final ViewItemStack entry = this.entries.get(i);
            if (entry.renderSize() == 0) continue;
            StackSizeRenderer.drawStackSize(
                entry.stack().relx,
                entry.stack().rely,
                entry.renderSize(),
                fontRenderer,
                TerminalFontSize.SMALL);
        }

        if (NEIClientUtils.getGuiContainer() instanceof GuiRecipe<?>gui) {
            final Point mouse = gui.getRecipeMousePosition(recipe);
            this.scrollArea.draw(mouse.x, mouse.y);
        }
    }

    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return Collections.emptyList();
    }

    @Override
    public List<PositionedStack> getOtherStacks(int recipe) {
        return this.otherStacks;
    }

    @Override
    public PositionedStack getResultStack(int recipe) {
        return null;
    }

    @Override
    public void onUpdate() {}

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
        return false;
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> currenttip, int recipe) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currenttip, int recipe) {
        if (stack == null || this.scrollArea == null) return currenttip;
        final int from = this.scrollArea.visibleFrom();
        final int to = this.scrollArea.visibleTo(this.entries.size());
        for (int i = from; i < to; i++) {
            final ViewItemStack entry = this.entries.get(i);
            if (!Platform.isSameItem(entry.stack().item, stack)) continue;
            currenttip.add(
                1,
                EnumChatFormatting.GRAY + GuiText.Stored.getLocal() + ": " + NUMBER_FORMAT.format(entry.count()));
            break;
        }
        return currenttip;
    }

    @Override
    public boolean keyTyped(GuiRecipe<?> gui, char keyChar, int keyCode, int recipe) {
        return false;
    }

    @Override
    public boolean mouseScrolled(final GuiRecipe<?> gui, final int scroll, final int recipe) {
        if (this.scrollArea == null) return false;
        final int step = (GuiScreen.isShiftKeyDown() ? this.rows : 1) * Math.max(1, Math.abs(scroll));
        return this.scrollArea.scrollRows(-Integer.signum(scroll) * step);
    }

    @Override
    public boolean mouseClicked(final GuiRecipe<?> gui, final int button, final int recipe) {
        if (this.scrollArea == null) return false;
        Point mouse = gui.getRecipeMousePosition(recipe);
        return this.scrollArea.onMouseClicked(button, mouse.x, mouse.y);
    }

    public record ViewItemStack(NEIScrollArea.CellStack stack, long renderSize, long count) {}
}
