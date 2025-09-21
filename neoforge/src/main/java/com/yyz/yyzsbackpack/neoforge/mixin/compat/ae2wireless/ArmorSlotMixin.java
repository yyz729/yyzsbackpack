package com.yyz.yyzsbackpack.neoforge.mixin.compat.ae2wireless;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import de.mari_023.ae2wtlib.wct.ArmorSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ArmorSlot.Armor.class)
public abstract class ArmorSlotMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 36), remap = false)
    private static int modifyOffhandSlot1(int original) {
        return original+ BackpackHelper.getSlotIndexOffset();
    }
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 37), remap = false)
    private static int modifyOffhandSlot2(int original) {
        return original+BackpackHelper.getSlotIndexOffset();
    }
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 38), remap = false)
    private static int modifyOffhandSlot3(int original) {
        return original+BackpackHelper.getSlotIndexOffset();
    }
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 39), remap = false)
    private static int modifyOffhandSlot4(int original) {
        return original+BackpackHelper.getSlotIndexOffset();
    }
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 40), remap = false)
    private static int modifyOffhandSlot(int original) {
        return original+BackpackHelper.getSlotIndexOffset();
    }
}
