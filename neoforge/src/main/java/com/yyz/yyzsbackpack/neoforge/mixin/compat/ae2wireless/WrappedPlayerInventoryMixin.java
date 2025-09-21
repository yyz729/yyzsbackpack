package com.yyz.yyzsbackpack.neoforge.mixin.compat.ae2wireless;

import com.yyz.yyzsbackpack.util.BackpackHelper;
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
        int offset = BackpackHelper.getSlotIndexOffset(); // 获取偏移量

        // 判断slotIndex是否是我们关心的五个索引之一
        if (slotIndex == 36 + offset || slotIndex == 37 + offset || slotIndex == 38 + offset ||
                slotIndex == 39 + offset || slotIndex == 40 + offset) {
            cir.setReturnValue(this.playerInventory.getItem(slotIndex));
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
