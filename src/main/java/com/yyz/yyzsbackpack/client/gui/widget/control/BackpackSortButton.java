package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.util.SortAlgorithms;
import com.yyz.yyzsbackpack.network.ModNetworking;
import com.yyz.yyzsbackpack.network.packets.control.SortRequestC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BackpackSortButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private static final int U_BASE = 80;
    private static final int HOVER_OFFSET = 10;

    private static int currentAlgorithmIndex = 0;
    private static final int ALGORITHM_COUNT = SortAlgorithms.NAMES.length;

    public BackpackSortButton(int x, int y) {
        super(x, y, 6, 6, Component.empty(), btn -> {}, DEFAULT_NARRATION);
    }

    public static void cycleAlgorithm() {
        currentAlgorithmIndex = (currentAlgorithmIndex + 1) % ALGORITHM_COUNT;
    }

    public static int getCurrentAlgorithmIndex() {
        return currentAlgorithmIndex;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int u = U_BASE + 3;
        int v = 3;
        if (this.isHovered()) {
            u += HOVER_OFFSET;
        }
        graphics.blit(TEXTURE, this.getX(), this.getY(), u, v, 6, 6, 256, 256);

        if (this.isHovered()) {
            String algoName = SortAlgorithms.NAMES[currentAlgorithmIndex];
            List<Component> tooltip = List.of(
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line1", Component.translatable(algoName)),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line2"),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line3"),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line4"),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line5"),
                    Component.translatable("yyzsbackpack.button.sort.tooltip.line6")
            );
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        int mask = buildTargetMask();
        if (mask != 0) {
            ModNetworking.CHANNEL.sendToServer(new SortRequestC2SPacket(currentAlgorithmIndex, mask));
        }
    }

    private int buildTargetMask() {
        boolean shift = Screen.hasShiftDown();
        boolean alt = Screen.hasAltDown();
        boolean ctrl = Screen.hasControlDown();

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