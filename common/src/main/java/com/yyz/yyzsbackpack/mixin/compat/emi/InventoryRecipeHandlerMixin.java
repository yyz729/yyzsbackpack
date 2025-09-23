package com.yyz.yyzsbackpack.mixin.compat.emi;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import dev.emi.emi.handler.InventoryRecipeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InventoryRecipeHandler.class)
public class InventoryRecipeHandlerMixin {
    @ModifyConstant(method = "getInputSources(Lnet/minecraft/world/inventory/InventoryMenu;)Ljava/util/List;", constant = @Constant(intValue = 36))
    private int adjustOffhandSlotPosition(int original) {
        return 36+ BackpackHelper.getMaxBackpackSize();
    }
}
