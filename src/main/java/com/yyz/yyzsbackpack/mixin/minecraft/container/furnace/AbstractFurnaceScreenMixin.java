package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin implements IBackpackOffset {

    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    @Override
    public int yyzsbackpack$getBackpackOffsetX(int segmentIndex) {
        if (getRecipeBookComponent().isVisible()) {
            AbstractFurnaceScreen<?> screen = (AbstractFurnaceScreen<?>) (Object) this;
            return BackpackScreenHelper.getConfigOffsetX(screen, segmentIndex);
        }
        return 0;
    }
    @Override
    public int yyzsbackpack$getBackpackOffsetY(int segmentIndex) {
        if (getRecipeBookComponent().isVisible()) {
            AbstractFurnaceScreen<?> screen = (AbstractFurnaceScreen<?>) (Object) this;
            return BackpackScreenHelper.getConfigOffsetY(screen, segmentIndex);
        }
        return 0;
    }

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractFurnaceScreen<?> screen = (AbstractFurnaceScreen<?>) (Object) this;
        BackpackScreenHelper.setupBackpackSlots(screen);
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(screen);
    }
}
