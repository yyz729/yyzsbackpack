package com.yyz.yyzsbackpack.mixin.minecraft.container.inventory;

import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.IScreenType;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin implements IBackpackOffset, IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "InventoryScreen";
    }

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
        BackpackScreenHelper.setupBackpackSlots((InventoryScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((InventoryScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((InventoryScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((InventoryScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((InventoryScreen) (Object) this, graphics, partialTick);

        BackpackScreenHelper.addBackpackControls((InventoryScreen) (Object) this);
    }

}