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
import top.theillusivec4.curios.client.gui.CuriosScreenV2;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;

@Mixin(CuriosScreenV2.class)
public abstract class CuriosScreenV2Mixin extends EffectRenderingInventoryScreen<CuriosContainerV2> {
    @Shadow(remap = false) public int panelWidth;

    public CuriosScreenV2Mixin(CuriosContainerV2 arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void shouldRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        BackpackCondition condition = (BackpackCondition) menu;
        condition.setRenderBackpack(true);
        condition.setBackpackOffset(-panelWidth, 0);

    }
}
