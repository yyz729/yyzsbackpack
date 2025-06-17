package com.yyz.yyzsbackpack.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EquipmentSlot.class)
public class EquipmentSlotMixin {
    @ModifyVariable(method = "getIndex(I)I", at = @At("HEAD"), argsOnly = true)
    private int modifyArmorIndex(int index) {
        return index + 55;
    }

}
