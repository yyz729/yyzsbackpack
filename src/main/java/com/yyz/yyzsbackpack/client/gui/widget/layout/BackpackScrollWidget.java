package com.yyz.yyzsbackpack.client.gui.widget.layout;

import com.yyz.yyzsbackpack.api.IBackpackScroll;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

public class BackpackScrollWidget extends AbstractWidget {
    private final AbstractContainerScreen<?> screen;
    private final IBackpackScroll scrollable;
    private final int segmentIndex;
    private boolean dragging = false;

    public BackpackScrollWidget(int x, int y, int width, int height,
                                AbstractContainerScreen<?> screen,
                                IBackpackScroll scrollable,
                                int segmentIndex) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
        this.scrollable = scrollable;
        this.segmentIndex = segmentIndex;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int maxScroll = scrollable.yyzsbackpack$getSegmentMaxScrollOffset(segmentIndex);
        if (maxScroll <= 0) return; // 无需滚动，不绘制

        // 半透明背景
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);

        int currentScroll = scrollable.yyzsbackpack$getSegmentScrollOffset(segmentIndex);
        float progress = (float) currentScroll / maxScroll;
        int sliderHeight = Math.max(10, this.height / (maxScroll + 1));
        int sliderY = this.getY() + (int) ((this.height - sliderHeight) * progress);
        graphics.fill(this.getX(), sliderY, this.getX() + this.width, sliderY + sliderHeight, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging && this.active && this.visible) {
            double relativeY = mouseY - this.getY();
            double progress = Math.max(0, Math.min(1, relativeY / this.height));
            int max = scrollable.yyzsbackpack$getSegmentMaxScrollOffset(segmentIndex);
            int newOffset = (int) (progress * max);
            scrollable.yyzsbackpack$setSegmentScrollOffset(segmentIndex, newOffset);
            // 刷新槽位
            BackpackScreenHelper.setupBackpackSlots(screen);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // 无需操作
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }
}