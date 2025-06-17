package com.yyz.yyzsbackpack.neoforge.mixin;


import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosMenu;

import static com.yyz.yyzsbackpack.BackpackManager.SLOT_TEXTURE;

@Mixin(CuriosScreen.class)
public abstract class CuriosScreenMixin extends AbstractRecipeBookScreen<CuriosMenu> {
    @Shadow(remap = false) public int panelWidth;

    public CuriosScreenMixin(CuriosMenu arg, RecipeBookComponent<?> arg2, Inventory arg3, Component arg4) {
        super(arg, arg2, arg3, arg4);
    }


    @Inject(method = "render", at = @At("HEAD"))
    private void shouldRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        BackpackRenderCondition condition = (BackpackRenderCondition) menu;
        condition.setRenderBackpack(true);
        condition.setBackpackOffset(-panelWidth, 0);

    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderForeground(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
        guiGraphics.blit(RenderType::guiTextured,SLOT_TEXTURE, leftPos + 8 + 69 -1,  topPos + 8 - 1 + 18 * 2, 0, 0, 18, 18, 18, 18);

    }
}
