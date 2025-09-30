package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EquipmentSlot.class)
public class EquipmentSlotMixin {
    @ModifyVariable(method = "getIndex(I)I", at = @At("HEAD"), argsOnly = true)
    private int modifyArmorIndex(int index) {
        return index + BackpackHelper.getSlotIndexOffset();
    }

}
