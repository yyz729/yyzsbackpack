package com.yyz.yyzsbackpack.client.gui.widget.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackVisible;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BackpackVisiblePickupButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack_buttons.png");
    private static final int X = 6;
    private static final int U_BASE = 0;
    private static final int VISIBLE_OFFSET = 1*X;
    private static final int PICKUP_OFFSET = 2*X;
    private static final int HOVER_OFFSET = 4*X;
    private final IBackpackVisible handler;

    public BackpackVisiblePickupButton(int x, int y, IBackpackVisible handler) {
        super(x, y, 6, 6, Component.empty(), btn -> {}, DEFAULT_NARRATION);
        this.handler = handler;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        boolean visible = handler.yyzsbackpack$isBackpackVisible();
        boolean preferPickup = BackpackMainConfig.getInstance().prefersBackpackPickup();

        int u = U_BASE;

        if (visible) {
            u += VISIBLE_OFFSET;
        }
        if (preferPickup) {
            u += PICKUP_OFFSET;
        }
        if (this.isHovered()) {
            u += HOVER_OFFSET;
        }
        graphics.blit(TEXTURE, this.getX(), this.getY(), u, 0, 4, 6, 192, 192);

        if (this.isHovered()) {
            List<Component> tooltip = List.of(
                    Component.translatable("yyzsbackpack.button.visiblepickup.line1", visible ? "Hide" : "Show"),
                    Component.translatable("yyzsbackpack.button.visiblepickup.line2", preferPickup ? "Disable" : "Enable")
            );
            String key = visible ? "yyzsbackpack.button.hide" : "yyzsbackpack.button.show";
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            BackpackMainConfig instance = BackpackMainConfig.getInstance();
            instance.setBackpackPickupPreference(!instance.prefersBackpackPickup());
        } else {
            handler.yyzsbackpack$toggleBackpackVisible();
        }
    }
}