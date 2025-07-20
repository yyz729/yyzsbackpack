package com.yyz.yyzsbackpack.mixin.compat.emi;

import dev.emi.emi.handler.CookingRecipeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CookingRecipeHandler.class)
public class CookingRecipeHandlerMixin {
    @ModifyConstant(method = "getInputSources(Lnet/minecraft/world/inventory/AbstractFurnaceMenu;)Ljava/util/List;", constant = @Constant(intValue = 36))
    private int adjustOffhandSlotPosition(int original) {
        return 36+54;
    }
}
