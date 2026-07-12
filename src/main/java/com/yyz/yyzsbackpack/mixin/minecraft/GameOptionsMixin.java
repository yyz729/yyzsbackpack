package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.client.BackpackKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Options.class)
public abstract class GameOptionsMixin {

    @Mutable
    @Final
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(at = @At("HEAD"), method = "load()V")
    private void onInit(CallbackInfo ci) {
        // 获取所有自定义按键
        List<KeyMapping> customKeys = BackpackKeyBinding.getAllBindings();

        // 收集现有按键的名称
        Set<String> existingNames = Arrays.stream(keyMappings)
                .map(KeyMapping::getName)
                .collect(Collectors.toSet());

        // 筛选出尚未存在的自定义按键
        List<KeyMapping> toAdd = customKeys.stream()
                .filter(k -> !existingNames.contains(k.getName()))
                .toList();

        if (toAdd.isEmpty()) return; // 没有需要添加的

        // 合并原数组和新按键
        KeyMapping[] newMappings = Arrays.copyOf(keyMappings, keyMappings.length + toAdd.size());
        for (int i = 0; i < toAdd.size(); i++) {
            newMappings[keyMappings.length + i] = toAdd.get(i);
        }
        keyMappings = newMappings;
    }
}