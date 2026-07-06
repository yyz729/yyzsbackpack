package com.yyz.yyzsbackpack.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BackpackTabWidget extends AbstractWidget {
    private final ItemStack icon;
    private final Runnable onSwitch;
    private final boolean selected;

    public BackpackTabWidget(int x, int y, ItemStack icon, boolean selected, Runnable onSwitch) {
        super(x, y, 28, 20, Component.empty());
        this.icon = icon;
        this.selected = selected;
        this.onSwitch = onSwitch;

        this.setTooltip(Tooltip.create(icon.getHoverName()));
    }

    /**
     * AbstractWidget 的渲染入口。
     */
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景色（选中 / 未选中）
        int color = selected ? 0xFF_AAAAAA : 0xFF_666666;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, color);

        // 绘制物品图标
        graphics.item(icon, getX() + 6, getY() + 2);

    }

    /**
     * 点击事件：参数为 MouseButtonEvent + 是否双击。
     */
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) { // 左键
            onSwitch.run();
        }
    }

    /**
     * 实现必须的抽象方法，使用默认叙述。
     */
    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}