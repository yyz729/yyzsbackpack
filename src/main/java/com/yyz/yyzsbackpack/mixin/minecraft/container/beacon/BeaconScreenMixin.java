package com.yyz.yyzsbackpack.mixin.minecraft.container.beacon;

import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public class BeaconScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "BeaconScreen";
    }
   

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((BeaconScreen) (Object) this);
    }


    @Inject(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onExtractBackgroundInvoke(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        BackpackScreenHelper.addBackpackBackground((BeaconScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((BeaconScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((BeaconScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((BeaconScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((BeaconScreen) (Object) this);
    }
}
