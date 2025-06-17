package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquipmentSlot.class)
public class EquipmentSlotMixin {
    @ModifyVariable(method = "getIndex(I)I", at = @At("HEAD"), argsOnly = true)
    private int modifyArmorIndex(int index) {
        return index + 55;
    }

}
