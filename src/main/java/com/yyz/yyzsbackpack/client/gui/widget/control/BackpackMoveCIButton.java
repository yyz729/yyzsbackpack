package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.network.control.MoveCToInventoryC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BackpackMoveCIButton extends Button {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");

    private static final int U_BASE = 100;
    private static final int HOVER_OFFSET = 20;

    public BackpackMoveCIButton(int x, int y) {
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

            List<FormattedCharSequence> tooltipLines = List.of(
                    Component.translatable("yyzsbackpack.button.moveci.line1").getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.moveci.line2").getVisualOrderText()
            );
            graphics.setTooltipForNextFrame(tooltipLines, mouseX, mouseY);
        }
    }
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        boolean shift = event.hasShiftDown();
        ClientPlayNetworking.send(new MoveCToInventoryC2SPacket(shift));
    }
}