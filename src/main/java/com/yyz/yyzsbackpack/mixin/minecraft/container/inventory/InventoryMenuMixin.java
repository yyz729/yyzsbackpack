package com.yyz.yyzsbackpack.mixin.minecraft.container.inventory;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.network.packets.control.MoveCToInventoryC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryMenu.class, priority = 499)
public class InventoryMenuMixin{

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        BackpackMenuHelper.addBackpackSlotsIfPresent(menu, inventory);

    }


    @Redirect(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/InventoryMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",
                    ordinal = 0
            )
    )
    private boolean redirectMoveToPlayer(InventoryMenu menu, ItemStack stack, int startSlot, int endSlot, boolean reverse, @Local(argsOnly = true) Player player) {
        return BackpackMenuHelper.moveItemStackToWithBackpack(menu, stack, startSlot, endSlot - 9, endSlot - 9, endSlot, BackpackMenuHelper.getBackpackSlotStart(menu), BackpackMenuHelper.getBackpackSlotStart(menu) + BackpackSlotHelper.getBackpackSize(player),reverse);
    }
}
