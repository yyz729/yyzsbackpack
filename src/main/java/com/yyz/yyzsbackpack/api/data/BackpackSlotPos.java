package com.yyz.yyzsbackpack.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BackpackSlotPos(int x, int y) {
    public static final Codec<BackpackSlotPos> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("x").forGetter(BackpackSlotPos::x),
            Codec.INT.fieldOf("y").forGetter(BackpackSlotPos::y)
        ).apply(instance, BackpackSlotPos::new)
    );
}