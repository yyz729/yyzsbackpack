package com.yyz.yyzsbackpack.mixin.minecraft.container.beacon;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public class BeaconScreenMixin{

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((BeaconScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((BeaconScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((BeaconScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((BeaconScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((BeaconScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((BeaconScreen) (Object) this);
    }
}
