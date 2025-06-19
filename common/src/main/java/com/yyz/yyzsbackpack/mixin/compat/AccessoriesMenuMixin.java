package com.yyz.yyzsbackpack.mixin.compat;

import io.wispforest.accessories.client.AccessoriesMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AccessoriesMenu.class)
public class AccessoriesMenuMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }
}
