package com.yyz.yyzsbackpack.mixin.minecraft.container.shulker;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreen.class)
public class ShulkerBoxScreenMixin implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "ShulkerBoxScreen";
    }
   

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ShulkerBoxScreen screen = (ShulkerBoxScreen)(Object)this;
        BackpackScreenHelper.setupBackpackSlots(screen);
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(screen);
    }
}
