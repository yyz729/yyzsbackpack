package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {
    public static final Holder<MobEffect> HEAVY;

    static {
        HEAVY = register("heavy", new HeavyEffect());
    }

    private static Holder<MobEffect> register(String path, MobEffect effect) {
        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, path),
                effect
        );
    }

    public static void register() {
        // 仅用于触发静态初始化
    }
}