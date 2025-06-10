package com.yyz.yyzsbackpack.mixin.screenhandler;

import com.yyz.yyzsbackpack.BackpackManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class FurnaceScreenHandlerMixin extends ScreenHandler {
    protected FurnaceScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }


    @Inject(method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/book/RecipeBookCategory;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V", at = @At("RETURN"))
    private void addSlots(ScreenHandlerType type, RecipeType recipeType, RecipeBookCategory category, int syncId, PlayerInventory inventory, Inventory inventory1, PropertyDelegate propertyDelegate, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory, 176, 166);
    }
}