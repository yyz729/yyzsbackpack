package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMountInventoryScreen.class)
public class AbstractMountInventoryScreenMixin {
   

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((AbstractMountInventoryScreen<?>) (Object) this);
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
        AbstractMountInventoryScreen<?> mountInventoryScreen = (AbstractMountInventoryScreen<?>) (Object) this;
        BackpackScreenHelper.addBackpackBackground(mountInventoryScreen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(mountInventoryScreen);
        BackpackScreenHelper.addBackpackScrollbar(mountInventoryScreen);
        BackpackScreenHelper.addBackpackTitle(mountInventoryScreen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(mountInventoryScreen);
    }
}
