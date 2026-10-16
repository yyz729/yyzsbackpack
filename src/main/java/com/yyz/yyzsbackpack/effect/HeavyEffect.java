package com.yyz.yyzsbackpack.effect;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HeavyEffect extends MobEffect {

    private static final Identifier HEAVY_MODIFIER_ID = 
            Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "effect.heavy");

    private static final Identifier HEAVY_JUMP_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "effect.heavy_jump");

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