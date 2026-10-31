package com.yyz.yyzsbackpack.mixin.minecraft.container.crafting;

import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin implements IBackpackOffset, IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "CraftingScreen";
    }

    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    @Override
    public int yyzsbackpack$getBackpackOffsetX(int segmentIndex) {
        if (getRecipeBookComponent().isVisible()) {
            CraftingScreen screen = (CraftingScreen) (Object) this;
            return BackpackScreenHelper.getConfigOffsetX(screen, segmentIndex);
        }
        return 0;
    }
    @Override
    public int yyzsbackpack$getBackpackOffsetY(int segmentIndex) {
        if (getRecipeBookComponent().isVisible()) {
            CraftingScreen screen = (CraftingScreen) (Object) this;
            return BackpackScreenHelper.getConfigOffsetY(screen, segmentIndex);
        }
        return 0;
    }

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CraftingScreen screen = (CraftingScreen) (Object) this;
        BackpackScreenHelper.setupBackpackSlots(screen);
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(screen);
    }
}
