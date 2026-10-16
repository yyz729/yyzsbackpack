package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.util.SortAlgorithms;
import com.yyz.yyzsbackpack.network.packets.control.SortRequestC2SPacket;
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

public class BackpackSortButton extends Button {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private static final int U_BASE = 80;
    private static final int HOVER_OFFSET = 10;

    private static int currentAlgorithmIndex = 0;
    private static final int ALGORITHM_COUNT = SortAlgorithms.NAMES.length;

    public BackpackSortButton(int x, int y) {
        super(x, y, 6, 6, Component.empty(), btn -> {}, DEFAULT_NARRATION);
    }

    /**
     * 切换到下一个算法
     */
    public static void cycleAlgorithm() {
        currentAlgorithmIndex = (currentAlgorithmIndex + 1) % ALGORITHM_COUNT;
    }

    public static int getCurrentAlgorithmIndex() {
        return  currentAlgorithmIndex;
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
            String algoName = SortAlgorithms.NAMES[currentAlgorithmIndex];
            List<FormattedCharSequence> tooltipLines = List.of(
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line1", Component.translatable(algoName)).getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line2").getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line3").getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line4").getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line5").getVisualOrderText(),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line6").getVisualOrderText()
            );
            graphics.setTooltipForNextFrame(tooltipLines, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (event.input() == 0) {
            int mask = buildTargetMask(event);
            if (mask != 0) {
                ClientPlayNetworking.send(new SortRequestC2SPacket(currentAlgorithmIndex, mask));
            }
        }
    }

    private int buildTargetMask(MouseButtonEvent event) {
        boolean shift = event.hasShiftDown();
        boolean alt = event.hasAltDown();
        boolean ctrl = event.hasControlDown();

        if (!shift && !alt && !ctrl) {
            return 7;
        }
        int mask = 0;
        if (shift) mask |= 1;
        if (alt)   mask |= 2;
        if (ctrl)  mask |= 4;
        return mask;
    }
}