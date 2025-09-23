package com.yyz.yyzsbackpack.fabric.mixin.compat.ae2wireless;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import de.mari_023.ae2wtlib.wct.WCTMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WCTMenu.class)
public class WCTMenuMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40), remap = false)
    private int modifyOffhandSlot(int original) {
        return original+ BackpackHelper.getSlotIndexOffset();
    }
}
