package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceMenu.class)
public class AbstractFurnaceMenuMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, ResourceKey<?> allowedInputs, RecipeBookType recipeBookType, int containerId, Inventory inventory, Container container, ContainerData data, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((AbstractFurnaceMenu)(Object)this, inventory);
    }
}
