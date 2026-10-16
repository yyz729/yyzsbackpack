package com.yyz.yyzsbackpack.mixin.minecraft.container.inventory;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "InventoryScreen";
    }

    public InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBookComponent, Inventory inventory, Component title) {
        super(menu, recipeBookComponent, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object)this;
        BackpackScreenHelper.setupBackpackSlots(screen);
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(screen);
    }
}