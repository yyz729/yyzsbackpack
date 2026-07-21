package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Backpack.MOD_ID);

    public static final DeferredHolder<MobEffect, HeavyEffect> HEAVY =
            MOB_EFFECTS.register("heavy", HeavyEffect::new);
}