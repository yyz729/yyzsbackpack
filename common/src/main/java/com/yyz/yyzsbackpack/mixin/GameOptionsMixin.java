package com.yyz.yyzsbackpack.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.client.BackpackKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.Arrays;

@Mixin(Options.class)
public abstract class GameOptionsMixin {

    @Mutable
    @Final
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V", shift = At.Shift.BEFORE))
    private void onInit(CallbackInfo ci) {
        for (KeyMapping kb : keyMappings) {
            if (kb.getName().equals("key.yyzsbackpack.sort")) {
                return;
            }
        }
        KeyMapping[] allKeys = Arrays.copyOf(keyMappings, keyMappings.length + 1);
        allKeys[allKeys.length - 1] = BackpackKeyBinding.KEY_SORT;
        keyMappings = allKeys;
    }
}