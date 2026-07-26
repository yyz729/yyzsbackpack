package com.yyz.yyzsbackpack.mixin.minecraft.container.horse;

import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryScreen.class)
public class HorseInventoryScreenMixin  implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "HorseInventoryScreen";
    }
   

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((HorseInventoryScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((HorseInventoryScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((HorseInventoryScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((HorseInventoryScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((HorseInventoryScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((HorseInventoryScreen) (Object) this);
    }
}
