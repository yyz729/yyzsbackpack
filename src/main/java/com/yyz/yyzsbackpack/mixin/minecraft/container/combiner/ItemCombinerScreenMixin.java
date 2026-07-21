package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCombinerScreen.class)
public class ItemCombinerScreenMixin {
   

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((ItemCombinerScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((ItemCombinerScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((ItemCombinerScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((ItemCombinerScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((ItemCombinerScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((ItemCombinerScreen) (Object) this);
    }
}
