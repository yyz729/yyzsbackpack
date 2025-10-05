package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EquipmentSlot.class)
public abstract class EquipmentSlotMixin {
    @Shadow public abstract EquipmentSlot.Type getType();

    @ModifyVariable(method = "getIndex(I)I", at = @At("HEAD"), argsOnly = true)
    private int modifyArmorIndex(int index) {
        if(getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            return index + BackpackHelper.getSlotIndexOffset();
        }
        return index;
    }

}
