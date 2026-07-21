package com.yyz.yyzsbackpack.mixin.minecraft.container.crafting;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin{


    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((CraftingMenu)(Object)this, inventory);
    }

    @Redirect(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/CraftingMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",
                    ordinal = 0
            )
    )
    private boolean redirectMoveToPlayer(CraftingMenu menu, ItemStack stack, int startSlot, int endSlot, boolean reverse, @Local(argsOnly = true) Player player) {
        return BackpackMenuHelper.moveItemStackToWithBackpack(menu, stack, startSlot, endSlot - 9, endSlot - 9, endSlot, BackpackMenuHelper.getBackpackSlotStart(menu), BackpackMenuHelper.getBackpackSlotStart(menu) + BackpackSlotHelper.getBackpackSize(player),reverse);
    }
}
