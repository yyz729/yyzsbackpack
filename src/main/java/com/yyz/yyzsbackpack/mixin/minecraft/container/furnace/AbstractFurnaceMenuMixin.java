package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceMenu.class)
public class AbstractFurnaceMenuMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, ResourceKey<?> allowedInputs, RecipeBookType recipeBookType, int containerId, Inventory inventory, Container container, ContainerData data, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((AbstractFurnaceMenu)(Object)this, inventory);
    }

    @Redirect(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",
                    ordinal = 0
            )
    )
    private boolean redirectMoveToPlayer(AbstractFurnaceMenu menu, ItemStack stack, int startSlot, int endSlot, boolean reverse, @Local(argsOnly = true) Player player) {
        return BackpackMenuHelper.moveItemStackToWithBackpack(menu, stack, startSlot, endSlot - 9, endSlot - 9, endSlot, BackpackMenuHelper.getBackpackSlotStart(menu), BackpackMenuHelper.getBackpackSlotStart(menu) + BackpackSlotHelper.getBackpackSize(player),reverse);
    }
}
