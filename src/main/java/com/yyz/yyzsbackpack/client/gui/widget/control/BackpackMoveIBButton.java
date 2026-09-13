package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import com.yyz.yyzsbackpack.network.packets.control.MoveIToBackpackC2SPacket;
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

public class BackpackMoveIBButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private static final int U_BASE = 40;
    private static final int HOVER_OFFSET = 20;

    public BackpackMoveIBButton(int x, int y) {
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
                    Component.translatable("yyzsbackpack.button.moveib.line1"),
                    Component.translatable("yyzsbackpack.button.moveib.line2"),
                    Component.translatable("yyzsbackpack.button.moveib.line3"),
                    Component.translatable("yyzsbackpack.button.moveib.line4")
            );
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        boolean shift = Screen.hasShiftDown();
        boolean ctrl  = Screen.hasControlDown();
        boolean alt   = Screen.hasAltDown();

        MoveMode mode;
        if (shift) {
            mode = MoveMode.ALL;
        } else if (ctrl) {
            mode = MoveMode.ONE_EACH;
        } else if (alt) {
            mode = MoveMode.FILL_MATCHING;
        } else {
            mode = MoveMode.MATCHING;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        MoveIToBackpackC2SPacket.write(buf, new MoveIToBackpackC2SPacket(mode));
        ClientPlayNetworking.send(MoveIToBackpackC2SPacket.ID, buf);
    }
}