package com.yyz.yyzsbackpack.mixin.compat.emi;

import dev.emi.emi.handler.StonecuttingRecipeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StonecuttingRecipeHandler.class)
public class StonecuttingRecipeHandlerMixin {
    @ModifyConstant(method = "getInputSources(Lnet/minecraft/world/inventory/StonecutterMenu;)Ljava/util/List;", constant = @Constant(intValue = 36))
    private int adjustOffhandSlotPosition(int original) {
        return 36+54;
    }
}
