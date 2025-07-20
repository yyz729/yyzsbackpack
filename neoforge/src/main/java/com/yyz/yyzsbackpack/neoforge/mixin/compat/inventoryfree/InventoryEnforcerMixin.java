package com.yyz.yyzsbackpack.neoforge.mixin.compat.inventoryfree;

import com.yyz.yyzsbackpack.BackpackManager;

import kirderf1.inventoryfree.slot_blocking.InventoryEnforcer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryEnforcer.class)
public class InventoryEnforcerMixin {

    @Redirect(method = "findAvailableSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"),remap = false)
    private static int modifyFindSlotMatchingUnusedItem(NonNullList<ItemStack> instance, Inventory inventory) {
        return BackpackManager.getBackpackSize(inventory.player);
    }
}
