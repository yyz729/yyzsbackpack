package com.yyz.yyzsbackpack.forge.mixin;

import com.yyz.yyzsbackpack.api.BackpackCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

@Mixin(CuriosScreen.class)
public abstract class CuriosScreenMixin extends EffectRenderingInventoryScreen<CuriosContainer>{
    @Shadow(remap = false) public boolean hasScrollBar;

    public CuriosScreenMixin(CuriosContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void shouldRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        BackpackCondition condition = (BackpackCondition) menu;
        condition.setRenderBackpack(true);

        CuriosApi.getCuriosHelper().getCuriosHandler(menu.player).ifPresent((handler) -> {
            int panelWidth = handler.getVisibleSlots() >0?26:0;

            if (((CuriosContainer)this.menu).hasCosmeticColumn()) {
                panelWidth += 40/2;
            }
            if (hasScrollBar) {
                panelWidth += 30/2;
            }
            condition.setBackpackOffset(-panelWidth, 0);
        });

    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderForeground(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
//        guiGraphics.blit(SLOT_TEXTURE,  leftPos + 8 + 69 -1,  topPos + 8 - 1 + 18 * 2, 0, 0, 18, 18, 18, 18);

    }
}
