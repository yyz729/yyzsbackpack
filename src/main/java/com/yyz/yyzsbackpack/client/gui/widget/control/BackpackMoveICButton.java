package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.network.packets.control.MoveIToContainerC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BackpackMoveICButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private static final int U_BASE = 110;
    private static final int HOVER_OFFSET = 20;

    public BackpackMoveICButton(int x, int y) {
        super(x, y, 6, 6, Component.empty(), btn -> {}, DEFAULT_NARRATION);
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
            List<Component> tooltip = List.of(
                    Component.translatable("yyzsbackpack.button.moveic.line1"),
                    Component.translatable("yyzsbackpack.button.moveic.line2")
            );
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        boolean shift = Screen.hasShiftDown();
        FriendlyByteBuf buf = PacketByteBufs.create();
        MoveIToContainerC2SPacket.write(buf, new MoveIToContainerC2SPacket(shift));
        ClientPlayNetworking.send(MoveIToContainerC2SPacket.ID, buf);
    }
}