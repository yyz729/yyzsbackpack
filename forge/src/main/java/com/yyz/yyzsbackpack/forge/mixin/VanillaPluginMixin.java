package com.yyz.yyzsbackpack.forge.mixin;

import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VanillaPlugin.class)
public class VanillaPluginMixin {
    @ModifyConstant(method = "registerRecipeTransferHandlers", constant = @Constant(intValue = 36),remap = false)
    private int offhandIndexChange(int og) {
        return 36+54;
    }
}
