package com.yyz.yyzsbackpack.mixin.minecraft.container.crafting;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends AbstractRecipeBookScreen<CraftingMenu> {
    public CraftingScreenMixin(CraftingMenu menu, RecipeBookComponent<?> recipeBookComponent, Inventory inventory, Component title) {
        super(menu, recipeBookComponent, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((CraftingScreen) (Object) this);
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
        BackpackScreenHelper.addBackpackBackground((CraftingScreen) (Object) this, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs((CraftingScreen) (Object) this);
        BackpackScreenHelper.addBackpackScrollbar((CraftingScreen) (Object) this);
        BackpackScreenHelper.addBackpackTitle((CraftingScreen) (Object) this, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls((CraftingScreen) (Object) this);
    }
}
