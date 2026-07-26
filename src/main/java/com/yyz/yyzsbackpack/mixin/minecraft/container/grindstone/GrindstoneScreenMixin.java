package com.yyz.yyzsbackpack.mixin.minecraft.container.grindstone;

import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreen.class)
public class GrindstoneScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "GrindstoneScreen";
    }
   

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((GrindstoneScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((GrindstoneScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((GrindstoneScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((GrindstoneScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((GrindstoneScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((GrindstoneScreen) (Object) this);
    }
}
