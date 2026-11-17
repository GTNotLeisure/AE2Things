package com.asdflj.ae2thing.nei;

import java.awt.Point;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

    private static final ResourceLocation SLOT_TEXTURE_LOCATION = new ResourceLocation("nei", "textures/slot.png");
    private static final int OFFSET_X = 2;
    private static final int INFO_OFFSET_Y = 4;
    private static final int ITEMS_OFFSET_Y = INFO_OFFSET_Y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2 + 6;
    private static final int COLUMNS = 8;
    private static final int SLOT_HEIGHT = 18;
    private static final int BOTTOM_MARGIN = 6;
    private static final int PAGE_HEADER_HEIGHT = 36;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private final ObjectList<ViewItemStack> entries = new ObjectArrayList<>();
    private final ObjectList<PositionedStack> positioned = new ObjectArrayList<>();
    private final Reference2ObjectMap<IAEStack<?>, PositionedStack> positionedCache = new Reference2ObjectOpenHashMap<>();

    private static int cachedScreenHeight = -1;
    private static int cachedMaxHeight = -1;
    private static int cachedMaxRows = 1;

    private NEIScrollArea scrollArea;
    private ItemStack cell;
    private long loadedVersion;
    private int rows = 1;
    private int rowsLimit = 1;
    private long usedTypes;
    private long totalTypes;
    private long usedBytes;
    private long totalBytes;
    private boolean unlimited;

    @Override
    public IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
        if (ingredients.length == 0 || !(ingredients[0] instanceof ItemStack cellItem)) return null;
        if (!(cell.getItem() instanceof BaseInfinityCellItem<?>storageCell)) {
            return null;
        }

        this.cell = cellItem;
        this.scrollArea = null;
        this.totalTypes = storageCell.getTotalTypes(cell);
        this.totalBytes = storageCell.getBytesLong(cell);
        this.unlimited = this.totalTypes >= Integer.MAX_VALUE;
        final int bytesPerType = storageCell.getBytesPerType(cell);

        this.usedTypes = 0;
        this.usedBytes = 0;
        for (final IAEStackType<?> type : Util.getTypes()) {
            final NBTTagCompound section = InfinityStorageCellSnapshot.section(cell, type);
            if (section == null) continue;

            final long types = section.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES);
            final long count = section.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT);
            if (types <= 0 && count <= 0) continue;
            this.usedTypes += types;
            this.usedBytes += count / Math.max(1, type.getAmountPerByte()) + types * (long) bytesPerType;
        }
        if (this.totalBytes > 0) this.usedBytes = Math.min(this.usedBytes, this.totalBytes);

        this.rebuild();
        return this;
    }

    private void rebuild() {
        if (this.cell == null) return;
        final int keepRow = this.scrollArea == null ? 0 : this.scrollArea.getFirstRow();
        final ObjectList<IAEStack<?>> contents = new ObjectArrayList<>();
        for (final IAEStackType<?> type : Util.getTypes()) {
            if (!hasEntries(type)) continue;
            contents.addAll(InfinityCellContentsCache.contents(this.cell, type));
        }
        contents.sort(
            Comparator.<IAEStack<?>>comparingLong(IAEStack::getStackSize)
                .reversed());

        this.entries.clear();
        this.positioned.clear();
        final Reference2ObjectMap<IAEStack<?>, PositionedStack> reused = new Reference2ObjectOpenHashMap<>();
        for (final IAEStack<?> aes : contents) {
            PositionedStack positionedStack = this.positionedCache.get(aes);
            if (positionedStack == null) {
                final ItemStack display = aes.getItemStackForNEI();
                final ItemStack single = aes.getItemStackForNEI(1);
                final boolean bakeCount = aes.isFluid() || aes.getStackSize() < Integer.MAX_VALUE;
                final ItemStack stack = bakeCount ? display : single;
                if (stack == null) continue;
                positionedStack = new PositionedStack(stack.copy(), 0, 0, false);
            }
            reused.put(aes, positionedStack);
            this.entries.add(
                new ViewItemStack(
                    positionedStack,
                    aes.isFluid() || aes.getStackSize() < Integer.MAX_VALUE ? 0 : aes.getStackSize(),
                    aes.getStackSize()));
            this.positioned.add(positionedStack);
        }
        this.positionedCache.clear();
        this.positionedCache.putAll(reused);
        this.rowsLimit = maxRows();
        this.rows = Math.clamp((this.entries.size() + COLUMNS - 1) / COLUMNS, 1, this.rowsLimit);
        this.scrollArea = new NEIScrollArea(OFFSET_X, ITEMS_OFFSET_Y, COLUMNS, this.rows);
        this.scrollArea.setStacks(this.positioned);
        this.scrollArea.scrollToRow(keepRow);
        this.loadedVersion = InfinityCellContentsCache.version();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasEntries(IAEStackType<?> type) {
        final NBTTagCompound section = InfinityStorageCellSnapshot.section(this.cell, type);
        if (section == null) return false;
        return section.getInteger(InfinityStorageCellSnapshot.NBT_SNAPSHOT_TYPES) > 0
            || section.getLong(InfinityStorageCellSnapshot.NBT_SNAPSHOT_COUNT) > 0;
    }

    private void requestMore() {
        if (this.cell == null) return;
        for (final IAEStackType<?> type : Util.getTypes()) {
            if (!hasEntries(type)) continue;
            InfinityCellContentsCache.requestMore(this.cell, type);
        }
    }

    private void ensureLayout() {
        this.rowsLimit = Math.clamp(this.rowsLimit, 1, maxRows());
        final int desired = Math.clamp((this.entries.size() + COLUMNS - 1) / COLUMNS, 1, this.rowsLimit);
        if (desired == this.rows && this.scrollArea != null) return;
        this.rows = desired;
        this.scrollArea = new NEIScrollArea(OFFSET_X, ITEMS_OFFSET_Y, COLUMNS, desired);
        this.scrollArea.setStacks(this.positioned);
    }

    private static int maxRows() {
        final Minecraft mc = Minecraft.getMinecraft();
        final int screenHeight = mc.currentScreen == null ? 240 : mc.currentScreen.height;
        final int maxHeight = NEIClientConfig.getIntSetting("inventory.guirecipe.maxHeight");
        if (screenHeight != cachedScreenHeight || maxHeight != cachedMaxHeight) {
            cachedScreenHeight = screenHeight;
            cachedMaxHeight = maxHeight;
            final int pageHeight = Math.min(screenHeight - 44 - GuiRecipeTabs.getTabHeight(), maxHeight);
            cachedMaxRows = Math
                .max(1, (pageHeight - PAGE_HEADER_HEIGHT - ITEMS_OFFSET_Y - BOTTOM_MARGIN) / SLOT_HEIGHT);
        }
        return cachedMaxRows;
    }

    @Override
    public int getRecipeHeight(int recipe) {
        this.ensureLayout();
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
        if (InfinityCellContentsCache.version() != this.loadedVersion) {
            this.rebuild();
        }
        this.requestMore();
        this.ensureLayout();
        if (this.scrollArea == null) return;

        if (NEIClientUtils.getGuiContainer() instanceof GuiRecipe<?>gui && this.scrollArea.isDragging()) {
            Point mouse = gui.getRecipeMousePosition(recipe);
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

        for (final ViewItemStack entry : this.entries) {
            if (entry.renderSize() == 0) continue;
            if (!NEIScrollArea.isShown(entry.stack)) continue;
            StackSizeRenderer.drawStackSize(
                entry.stack.relx,
                entry.stack.rely,
                entry.renderSize,
                fontRenderer,
                TerminalFontSize.SMALL);
        }

        if (this.scrollArea != null && NEIClientUtils.getGuiContainer() instanceof GuiRecipe<?>gui) {
            Point mouse = gui.getRecipeMousePosition(recipe);
            this.scrollArea.draw(mouse.x, mouse.y);
        }
    }

    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return Collections.emptyList();
    }

    @Override
    public List<PositionedStack> getOtherStacks(int recipe) {
        return this.positioned;
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
        if (stack == null) return currenttip;
        for (final ViewItemStack entry : this.entries) {
            if (!NEIScrollArea.isShown(entry.stack)) continue;
            if (!entry.stack.item.equals(stack)) continue;
            currenttip
                .add(1, EnumChatFormatting.GRAY + GuiText.Stored.getLocal() + ": " + NUMBER_FORMAT.format(entry.count));
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
        Point mouse = gui.getRecipeMousePosition(recipe);
        return this.scrollArea.onMouseWheel(scroll, mouse.x, mouse.y);
    }

    @Override
    public boolean mouseClicked(final GuiRecipe<?> gui, final int button, final int recipe) {
        if (this.scrollArea == null) return false;
        Point mouse = gui.getRecipeMousePosition(recipe);
        return this.scrollArea.onMouseClicked(button, mouse.x, mouse.y);
    }

    public record ViewItemStack(PositionedStack stack, long renderSize, long count) {}
}
