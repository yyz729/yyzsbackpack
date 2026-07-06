package com.yyz.yyzsbackpack.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record LayoutSegment(
        int startSlot,
        LayoutOrder order,
        Optional<Integer> startX,
        Optional<Integer> startY,
        Optional<Integer> columns,
        Optional<Integer> rows,
        Optional<List<BackpackSlotPos>> customPositions
) {
    public int getSlotCount() {
        if (order == LayoutOrder.CUSTOM) {
            return customPositions.orElseThrow(() ->
                new IllegalStateException("missing customPositions")).size();
        } else {
            return columns.orElseThrow(() ->
                new IllegalStateException("missing columns")) *
                   rows.orElseThrow(() ->
                new IllegalStateException("missing rows"));
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
            LayoutOrder.CODEC.fieldOf("order").forGetter(LayoutSegment::order),
            Codec.INT.optionalFieldOf("startX").forGetter(LayoutSegment::startX),
            Codec.INT.optionalFieldOf("startY").forGetter(LayoutSegment::startY),
            Codec.INT.optionalFieldOf("columns").forGetter(LayoutSegment::columns),
            Codec.INT.optionalFieldOf("rows").forGetter(LayoutSegment::rows),
            Codec.list(BackpackSlotPos.CODEC).optionalFieldOf("customPositions").forGetter(LayoutSegment::customPositions)
        ).apply(instance, LayoutSegment::new)
    );
}