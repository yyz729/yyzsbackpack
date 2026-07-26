package com.yyz.yyzsbackpack.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record LayoutSegment(
        int startSlot,
        int endSlot,
        LayoutOrder order,
        Optional<Integer> startX,
        Optional<Integer> startY,
        Optional<Integer> columns,
        Optional<Integer> rows,
        Optional<List<BackpackSlotPos>> customPositions,
        Optional<Identifier> backgroundTexture,
        Optional<Integer> backgroundX,
        Optional<Integer> backgroundY
) {
    public int getSlotCount() {
        if (order == LayoutOrder.CUSTOM) {
            return customPositions.orElseThrow(() ->
                new IllegalStateException("missing customPositions")).size();
        } else {
            return endSlot - startSlot;
        }
    }

    public int getEffectiveStartX() {
        if (order == LayoutOrder.CUSTOM) return 0;
        return startX.orElseThrow(() ->
            new IllegalStateException("missing startX"));
    }

    public int getEffectiveStartY() {
        if (order == LayoutOrder.CUSTOM) return 0;
        return startY.orElseThrow(() ->
            new IllegalStateException("missing startY"));
    }

    public static final Codec<LayoutSegment> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("startSlot").forGetter(LayoutSegment::startSlot),
            Codec.INT.fieldOf("endSlot").forGetter(LayoutSegment::endSlot),
            LayoutOrder.CODEC.fieldOf("order").forGetter(LayoutSegment::order),
            Codec.INT.optionalFieldOf("startX").forGetter(LayoutSegment::startX),
            Codec.INT.optionalFieldOf("startY").forGetter(LayoutSegment::startY),
            Codec.INT.optionalFieldOf("columns").forGetter(LayoutSegment::columns),
            Codec.INT.optionalFieldOf("rows").forGetter(LayoutSegment::rows),
            Codec.list(BackpackSlotPos.CODEC).optionalFieldOf("customPositions").forGetter(LayoutSegment::customPositions),
            Identifier.CODEC.optionalFieldOf("backgroundTexture").forGetter(LayoutSegment::backgroundTexture),
            Codec.INT.optionalFieldOf("backgroundX").forGetter(LayoutSegment::backgroundX),
            Codec.INT.optionalFieldOf("backgroundY").forGetter(LayoutSegment::backgroundY)
        ).apply(instance, LayoutSegment::new)
    );
}