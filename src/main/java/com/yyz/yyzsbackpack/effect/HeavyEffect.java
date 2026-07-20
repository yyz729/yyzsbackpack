package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HeavyEffect extends MobEffect {

    private static final ResourceLocation HEAVY_MODIFIER_ID = 
            ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "effect.heavy");

    private static final ResourceLocation HEAVY_JUMP_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "effect.heavy_jump");

    public HeavyEffect() {
        super(MobEffectCategory.HARMFUL, 0x4C3F2D);
        // 为移动速度添加 -60% 的修饰符
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            HEAVY_MODIFIER_ID,
            -0.5,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.JUMP_STRENGTH,
                HEAVY_JUMP_MODIFIER_ID,
                -0.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}