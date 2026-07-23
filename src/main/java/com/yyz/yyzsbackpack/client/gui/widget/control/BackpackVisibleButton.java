package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackVisible;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BackpackVisibleButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private final IBackpackVisible handler;

    public BackpackVisibleButton(int x, int y, IBackpackVisible handler) {
        super(x, y, 6, 6, Component.empty(), btn -> handler.yyzsbackpack$toggleBackpackVisible(), DEFAULT_NARRATION);
        this.handler = handler;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        boolean visible = handler.yyzsbackpack$isBackpackVisible();
        int u = (visible ? 10 : 0) + 3;
        int v = 3;
        if (this.isHovered()) {
            u += 20;
        }
        graphics.blit(TEXTURE, this.getX(), this.getY(), u, v, 4, 6, 256, 256);

        if (this.isHovered()) {
            String key = visible ? "yyzsbackpack.button.hide" : "yyzsbackpack.button.show";
            List<Component> tooltip = List.of(Component.translatable(key));
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }
}