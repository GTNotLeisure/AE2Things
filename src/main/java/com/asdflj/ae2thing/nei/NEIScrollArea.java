package com.asdflj.ae2thing.nei;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.PositionedStack;
import codechicken.nei.scroll.ScrollBar;
import codechicken.nei.scroll.ScrollContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * inspired by <a href="https://github.com/GTNewHorizons/GT5-Unofficial/pull/8675">GTNewHorizons/GT5-Unofficial#8675</a>
 */
public class NEIScrollArea {

    private static final int SLOT_SIZE = 18;
    private static final int TRACK_GAP = 2;
    private static final int HIDDEN_POSITION = -10_000;

    private final int x;
    private final int y;
    private final int columns;
    private final int visibleRows;
    private final ObjectList<CellStack> stacks = new ObjectArrayList<>();
    private final AreaContainer container;

    private int totalRows;
    private int firstRow;

    public NEIScrollArea(int x, int y, int columns, int visibleRows) {
        this.x = x;
        this.y = y;
        this.columns = columns;
        this.visibleRows = visibleRows;
        this.container = new AreaContainer(x, y, columns * SLOT_SIZE, visibleRows * SLOT_SIZE);
    }

    public int getVisibleRows() {
        return this.visibleRows;
    }

    public int getTotalRows() {
        return this.totalRows;
    }

    public int visibleFrom() {
        return Math.max(0, this.firstRow) * this.columns;
    }

    public int visibleTo(int size) {
        return Math.min(size, this.visibleFrom() + this.visibleRows * this.columns);
    }

    public NEIScrollArea setStacks(Collection<? extends CellStack> newStacks) {
        final int keep = this.firstRow;
        this.stacks.clear();
        this.stacks.addAll(newStacks);
        this.totalRows = (this.stacks.size() + this.columns - 1) / this.columns;
        this.container.setActualHeight(this.totalRows * SLOT_SIZE);
        this.firstRow = -1;
        this.setFirstRow(keep);
        return this;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isScrollable() {
        return this.totalRows > this.visibleRows;
    }

    public boolean isDragging() {
        return this.container.getVerticalScroll()
            .isMouseDragged();
    }

    public int getFirstRow() {
        return this.firstRow;
    }

    public boolean scrollRows(int rows) {
        if (rows == 0 || !this.isScrollable()) return false;
        final int before = this.firstRow;
        this.setFirstRow(this.firstRow + rows);
        return this.firstRow != before;
    }

    public boolean onMouseClicked(int button, int mouseX, int mouseY) {
        if (!this.isScrollable() || button != 0) return false;
        if (!this.container.getVerticalScroll()
            .onClick(this.container, mouseX, mouseY, button)) return false;
        this.syncFromContainer();
        return true;
    }

    public void updateDrag(boolean leftButtonDown, int mouseX, int mouseY) {
        final ScrollBar bar = this.container.getVerticalScroll();
        if (leftButtonDown) {
            bar.onMouseDragged(this.container, mouseX, mouseY, 0, 0L);
            this.syncFromContainer();
        } else {
            bar.onClickReleased(this.container, mouseX, mouseY, 0);
        }
    }

    public void draw(int mouseX, int mouseY) {
        if (!this.isScrollable()) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        this.container.getVerticalScroll()
            .draw(this.container, mouseX, mouseY, 1f);
        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void setFirstRow(int row) {
        final int maxRow = Math.max(0, this.totalRows - this.visibleRows);
        final int nextRow = Math.clamp(row, 0, maxRow);
        this.container.setVerticalScrollOffset(nextRow * SLOT_SIZE);

        if (nextRow == this.firstRow) return;

        this.firstRow = nextRow;
        this.layout();
    }

    private void syncFromContainer() {
        this.setFirstRow(Math.round(this.container.getVerticalScrollOffset() / (float) SLOT_SIZE));
    }

    private void layout() {
        for (int i = 0; i < this.stacks.size(); i++) {
            final CellStack stack = this.stacks.get(i);
            final int visibleRow = i / this.columns - this.firstRow;
            final boolean visible = visibleRow >= 0 && visibleRow < this.visibleRows;
            stack.visible = visible;
            if (visible) {
                stack.relx = this.x + (i % this.columns) * SLOT_SIZE + 1;
                stack.rely = this.y + visibleRow * SLOT_SIZE + 1;
            } else {
                stack.relx = HIDDEN_POSITION;
                stack.rely = HIDDEN_POSITION;
            }
        }
    }

    private static final class AreaContainer extends ScrollContainer {

        AreaContainer(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.w = width;
            this.h = height;
            this.setVerticalScroll(
                ScrollBar.defaultVerticalBar()
                    .setOverflowType(ScrollBar.OverflowType.AUTO)
                    .setTrackPadding(TRACK_GAP, 0, 0, 0));
        }

        void setActualHeight(int height) {
            this.actualHeight = height;
        }
    }

    public static class CellStack extends PositionedStack {

        private boolean visible;

        public CellStack(ItemStack stack) {
            super(stack, HIDDEN_POSITION, HIDDEN_POSITION, false);
        }

        public boolean isVisible() {
            return this.visible;
        }

        @Override
        public void draw(int mouseX, int mouseY) {
            if (this.visible) super.draw(mouseX, mouseY);
        }
    }
}
