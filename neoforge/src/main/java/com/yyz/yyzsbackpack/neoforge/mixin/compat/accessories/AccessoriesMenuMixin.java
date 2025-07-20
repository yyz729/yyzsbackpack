package com.yyz.yyzsbackpack.neoforge.mixin.compat.accessories;

import com.yyz.yyzsbackpack.base.BackpackCondition;
import io.wispforest.accessories.menu.variants.AccessoriesMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AccessoriesMenu.class)
public abstract class AccessoriesMenuMixin implements BackpackCondition {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

}
