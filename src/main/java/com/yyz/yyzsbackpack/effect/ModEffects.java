package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    // 注册器
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Backpack.MOD_ID);

    // 注册药水效果
    public static final RegistryObject<HeavyEffect> HEAVY =
            MOB_EFFECTS.register("heavy", HeavyEffect::new);
}