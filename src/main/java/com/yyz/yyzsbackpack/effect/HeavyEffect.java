package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class HeavyEffect extends MobEffect {

    private static final ResourceLocation HEAVY_MODIFIER_ID =
            new ResourceLocation(Backpack.MOD_ID, "effect.heavy");

    private static final ResourceLocation HEAVY_JUMP_MODIFIER_ID =
            new ResourceLocation(Backpack.MOD_ID, "effect.heavy_jump");

    public HeavyEffect() {
        super(MobEffectCategory.HARMFUL, 0x4C3F2D);

        String speedUUID = "5063B5F7-AB82-4963-A931-1791E81FA656";
        String jumpUUID = "4B5DD794-275C-4EC8-9CDB-7D6C8D168C96";

        // 移动速度 -50%（ADD_MULTIPLIED_TOTAL 对应 MULTIPLY_TOTAL）
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                speedUUID,
                -0.5,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // 跳跃力量 -50%
        this.addAttributeModifier(
                Attributes.JUMP_STRENGTH,
                jumpUUID,
                -0.5,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}