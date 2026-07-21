package com.yyz.yyzsbackpack.mixin.minecraft.container.crafter;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterScreen.class)
public class CrafterScreenMixin {
   

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((CrafterScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((CrafterScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((CrafterScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((CrafterScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((CrafterScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((CrafterScreen) (Object) this);
    }
}
