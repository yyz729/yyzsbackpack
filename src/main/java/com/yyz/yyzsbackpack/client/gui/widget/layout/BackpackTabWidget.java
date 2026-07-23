package com.yyz.yyzsbackpack.client.gui.widget.layout;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BackpackTabWidget extends AbstractWidget {
    private final ItemStack icon;
    private final Runnable onSwitch;
    private final boolean selected;

    public BackpackTabWidget(int x, int y, ItemStack icon, boolean selected, Runnable onSwitch) {
        super(x, y, 9, 9, Component.empty());
        this.icon = icon;
        this.selected = selected;
        this.onSwitch = onSwitch;
        this.setTooltip(Tooltip.create(icon.getHoverName()));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(getX(), getY(), 0);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);

        // 背景（尺寸 18x18，缩放后显示为 9x9）
        int color = selected ? 0xFFAAAAAA : 0xFF666666;
        graphics.fill(0, 0, 18, 18, color);

        // 绘制物品（位置 1,1，缩放后对应实际 0.5,0.5，但物品渲染会按屏幕坐标，需保证在背景内）
        graphics.renderItem(icon, 1, 1);

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
            onSwitch.run();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean isSelected() {
        return selected;
    }
}