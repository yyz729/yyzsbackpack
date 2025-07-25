package com.yyz.yyzsbackpack.neoforge.mixin.compat.curios;


import com.yyz.yyzsbackpack.base.BackpackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

@Mixin(CuriosScreen.class)
public abstract class CuriosScreenMixin extends EffectRenderingInventoryScreen<CuriosContainer>{
    @Shadow(remap = false) public int panelWidth;

    public CuriosScreenMixin(CuriosContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void shouldRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        BackpackMenu condition = (BackpackMenu) menu;
        condition.setBackpackVisible(true);
        condition.setBackpackGuiPos(-panelWidth, 0);
    }
}
