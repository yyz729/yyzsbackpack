package com.yyz.yyzsbackpack.mixin.minecraft.container.cartography;

import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartographyTableScreen.class)
public class CartographyTableScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "CartographyTableScreen";
    }
   

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((CartographyTableScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((CartographyTableScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((CartographyTableScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((CartographyTableScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((CartographyTableScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((CartographyTableScreen) (Object) this);
    }
}
