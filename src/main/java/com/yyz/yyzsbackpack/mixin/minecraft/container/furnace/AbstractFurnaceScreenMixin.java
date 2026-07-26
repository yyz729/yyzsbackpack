package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.IScreenType;
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
public abstract class AbstractFurnaceScreenMixin implements IBackpackOffset{

    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    @Override
    public int yyzsbackpack$getBackpackOffsetX() {
        if (getRecipeBookComponent().isVisible()) {
            return -180;
        }
        return 0;
    }
    @Override
    public int yyzsbackpack$getBackpackOffsetY() {
        return 0;
    }

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((AbstractFurnaceScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((AbstractFurnaceScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((AbstractFurnaceScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((AbstractFurnaceScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((AbstractFurnaceScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((AbstractFurnaceScreen) (Object) this);
    }
}
