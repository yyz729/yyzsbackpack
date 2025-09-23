package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.client.BackpackRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public  abstract class InventoryRenderConditionMixin extends EffectRenderingInventoryScreen<InventoryMenu> {


    @Shadow public abstract RecipeBookComponent getRecipeBookComponent();

    public InventoryRenderConditionMixin(InventoryMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderForeground(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
        BackpackRenderer.renderEquipSlotBackground(menu,guiGraphics,leftPos + 8 + 69 -1,  topPos + 8 - 1 + 18 * 2);
    }


    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(CallbackInfo ci) {
        ((BackpackMenu)menu).setBackpackVisible(!getRecipeBookComponent().isVisible());
    }
}
