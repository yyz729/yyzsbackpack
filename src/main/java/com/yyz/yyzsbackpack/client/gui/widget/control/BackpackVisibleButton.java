package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BackpackVisibleButton extends Button {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private final IBackpackToggle handler;

    public BackpackVisibleButton(int x, int y, IBackpackToggle handler) {
        super(x, y, 6, 6, Component.empty(), btn -> handler.yyzsbackpack$toggleBackpackVisible(), DEFAULT_NARRATION);
        this.handler = handler;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean visible = handler.yyzsbackpack$isBackpackVisible();
        // 可见时箭头向右，隐藏时箭头向左
        int u = (visible ? 10 : 0) + 3;
        int v = 3;
        if (this.isHovered()) {
            u += 20;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), u, v, 4, 6, 256, 256);
        if (this.isHovered()) {
            String key = visible ? "yyzsbackpack.button.hide" : "yyzsbackpack.button.show";
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable(key), mouseX, mouseY);
        }
    }
}