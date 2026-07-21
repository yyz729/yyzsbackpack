package com.yyz.yyzsbackpack.mixin.minecraft.container.chest;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestMenu.class)
public class ChestMenuMixin{

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, int containerId, Inventory inventory, Container container, int rows, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((ChestMenu)(Object)this, inventory);
    }

    @Redirect(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/ChestMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",
                    ordinal = 0
            )
    )
    private boolean redirectMoveToPlayer(ChestMenu menu, ItemStack stack, int startSlot, int endSlot, boolean reverse ,@Local(argsOnly = true) Player player) {
        return BackpackMenuHelper.moveItemStackToWithBackpack(menu, stack, startSlot, startSlot + 27, startSlot + 27, startSlot + 36, BackpackMenuHelper.getBackpackSlotStart(menu), BackpackMenuHelper.getBackpackSlotStart(menu) + BackpackSlotHelper.getBackpackSize(player),reverse);
    }

}
