package com.yyz.yyzsbackpack.mixin.minecraft.container.hopper;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperScreen.class)
public class HopperScreenMixin{

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((HopperScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((HopperScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((HopperScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((HopperScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((HopperScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((HopperScreen) (Object) this);
    }
}
