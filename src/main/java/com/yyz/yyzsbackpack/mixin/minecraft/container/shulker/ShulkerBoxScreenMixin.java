package com.yyz.yyzsbackpack.mixin.minecraft.container.shulker;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreen.class)
public class ShulkerBoxScreenMixin {

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((ShulkerBoxScreen) (Object) this);
    }


    @Inject(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onExtractBackgroundInvoke(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        BackpackScreenHelper.addBackpackBackground((ShulkerBoxScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((ShulkerBoxScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((ShulkerBoxScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((ShulkerBoxScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((ShulkerBoxScreen) (Object) this);
    }
}
