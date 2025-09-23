package com.yyz.yyzsbackpack.fabric.mixin.compat.ae2wireless;


import com.yyz.yyzsbackpack.util.BackpackHelper;
import de.mari_023.ae2wtlib.terminal.WrappedPlayerInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WrappedPlayerInventory.class)
public class WrappedPlayerInventoryMixin {
    @Shadow(remap = false) @Final private Inventory playerInventory;

    @Inject(method = "getStackInSlot", at = @At("RETURN"),remap = false, cancellable = true)
    private void getStackInSlot(int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        int offset = BackpackHelper.getSlotIndexOffset();
        if (slotIndex == 36 + offset ||
                slotIndex == 37 + offset ||
                slotIndex == 38 + offset ||
                slotIndex == 39 + offset ||
                slotIndex == 40 + offset) {
            cir.setReturnValue(this.playerInventory.getItem(slotIndex));
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
