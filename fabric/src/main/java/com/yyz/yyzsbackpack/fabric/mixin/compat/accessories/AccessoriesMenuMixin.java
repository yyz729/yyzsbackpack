package com.yyz.yyzsbackpack.fabric.mixin.compat.accessories;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import io.wispforest.accessories.menu.variants.AccessoriesMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AccessoriesMenu.class)
public abstract class AccessoriesMenuMixin implements BackpackMenu {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

}
