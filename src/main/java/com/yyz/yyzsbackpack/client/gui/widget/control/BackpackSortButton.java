package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BackpackSortButton extends Button {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");

    private static final int U_BASE = 80;
    private static final int HOVER_OFFSET = 10;

    public BackpackSortButton(int x, int y) {
        super(x, y, 6, 6, Component.empty(), btn -> {}, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int u = U_BASE + 3;
        int v = 3;
        if (this.isHovered()) {
            u += HOVER_OFFSET;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), u, v, 6, 6, 256, 256);

        if (this.isHovered()) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                Component.translatable("yyzsbackpack.button.sort"),
                mouseX, mouseY
            );
        }
    }
}