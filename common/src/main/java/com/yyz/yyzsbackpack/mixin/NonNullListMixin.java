package com.yyz.yyzsbackpack.mixin;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.AbstractList;

@Mixin(NonNullList.class)
public abstract class NonNullListMixin<E> extends AbstractList<E> {

    @Shadow public abstract int size();

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void handleBackpackSwap1(int i, CallbackInfoReturnable<E> cir) {
        if(i >= size()) {
            cir.setReturnValue(get(0));
        }
    }
}
