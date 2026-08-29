package com.yyz.yyzsbackpack.mixin.minecraft.container.brewing;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandScreen.class)
public class BrewingStandScreenMixin {

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((BrewingStandScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((BrewingStandScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((BrewingStandScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((BrewingStandScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((BrewingStandScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((BrewingStandScreen) (Object) this);
    }
}
