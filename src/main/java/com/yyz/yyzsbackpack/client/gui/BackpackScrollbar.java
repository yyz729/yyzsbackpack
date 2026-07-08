package com.yyz.yyzsbackpack.client.gui;

import com.yyz.yyzsbackpack.api.IBackpackScroll;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BackpackScrollbar extends AbstractWidget {
    private final AbstractContainerScreen<?> screen;
    private final IBackpackScroll scrollable;
    private boolean dragging = false;

    public BackpackScrollbar(int x, int y, int width, int height, AbstractContainerScreen<?> screen, IBackpackScroll scrollable) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
        this.scrollable = scrollable;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 背景（半透明）
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        // 滑块
        int maxScroll = scrollable.getMaxScrollOffset();
        if (maxScroll <= 0) return; // 无需滚动，不绘制滑块

        int currentScroll = scrollable.getScrollOffset();
        float progress = maxScroll > 0 ? (float) currentScroll / maxScroll : 0;
        int sliderHeight = Math.max(10, this.height / (maxScroll + 1));
        int sliderY = this.getY() + (int)((this.height - sliderHeight) * progress);
        graphics.fill(this.getX(), sliderY, this.getX() + this.width, sliderY + sliderHeight, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.isMouseOver(event.x(), event.y())) {
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging && this.active && this.visible) {
            double relativeY = event.y() - this.getY();
            double progress = Math.max(0, Math.min(1, relativeY / this.height));
            int newOffset = (int)(progress * scrollable.getMaxScrollOffset());
            scrollable.setScrollOffset(newOffset);
            // 刷新槽位
            BackpackScreenHelper.setupBackpackSlots(screen);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging) {
            this.dragging = false;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        // 无操作
    }
}