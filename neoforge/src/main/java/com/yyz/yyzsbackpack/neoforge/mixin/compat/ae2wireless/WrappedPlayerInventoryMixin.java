package com.yyz.yyzsbackpack.neoforge.mixin.compat.ae2wireless;

import com.yyz.yyzsbackpack.util.SlotManager;
import de.mari_023.ae2wtlib.wct.WrappedPlayerInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WrappedPlayerInventory.class)
public class WrappedPlayerInventoryMixin {
    @Shadow(remap = false) @Final private Inventory playerInventory;

    @Inject(method = "getStackInSlot", at = @At("RETURN"),remap = false, cancellable = true)
    private void getStackInSlot(int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack var10000 = switch (slotIndex) {
            case 36+55, 37+55, 38+55, 39+55, 40+55 -> this.playerInventory.getItem(slotIndex);
            default -> ItemStack.EMPTY;
        };


        cir.setReturnValue(var10000);
    }
}
