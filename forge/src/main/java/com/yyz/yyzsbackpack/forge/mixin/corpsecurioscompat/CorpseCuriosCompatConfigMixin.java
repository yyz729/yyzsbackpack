package com.yyz.yyzsbackpack.forge.mixin.corpsecurioscompat;

import com.leclowndu93150.corpsecurioscompat.Config;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Config.class)
public class CorpseCuriosCompatConfigMixin {

    @Inject(method = "isItemBlacklisted", at = @At("HEAD"), cancellable = true,remap = false)
    private static void isItemBlacklisted(Item item, CallbackInfoReturnable<Boolean> cir) {
        if(item instanceof BackpackItem){
            cir.setReturnValue(true);
        }
    }
}
