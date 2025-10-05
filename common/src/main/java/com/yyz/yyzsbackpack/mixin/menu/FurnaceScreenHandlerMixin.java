package com.yyz.yyzsbackpack.mixin.menu;

import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceMenu.class)
public abstract class FurnaceScreenHandlerMixin extends AbstractContainerMenu {


    protected FurnaceScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void addSlots(MenuType<?> menuType, RecipeType<?> recipeType, RecipeBookType recipeBookType, int i, Inventory inventory, Container container, ContainerData containerData, CallbackInfo ci) {
        SlotManager.addBackpackInventorySlots(this,inventory);
    }
}