package com.yyz.yyzsbackpack.mixin.minecraft.container.stonecutter;

import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterScreen.class)
public class StonecutterScreenMixin  implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "StonecutterScreen";
    }
   

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((StonecutterScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((StonecutterScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((StonecutterScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((StonecutterScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((StonecutterScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((StonecutterScreen) (Object) this);
    }
}
