package com.yyz.yyzsbackpack.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BackpackData(
        String type,
        int size,
//        ResourceLocation guiTexture,
//        int backgroundX,
//        int backgroundY,
        boolean forceServer,
        int maxVisibleTabs,
        List<LayoutSegment> segments
) {
    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(BackpackData::type),
                    Codec.INT.optionalFieldOf("size", 0).forGetter(BackpackData::size),
//                    ResourceLocation.CODEC.fieldOf("guiTexture").forGetter(BackpackData::guiTexture),
//                    Codec.INT.fieldOf("backgroundX").forGetter(BackpackData::backgroundX),
//                    Codec.INT.fieldOf("backgroundY").forGetter(BackpackData::backgroundY),
                    Codec.BOOL.optionalFieldOf("force_server", false).forGetter(BackpackData::forceServer),
                    Codec.INT.optionalFieldOf("maxVisibleTabs", -1).forGetter(BackpackData::maxVisibleTabs),
                    Codec.list(LayoutSegment.CODEC).fieldOf("segments").forGetter(BackpackData::segments)
            ).apply(instance, BackpackData::new)
    );

    public CompoundTag toNbt() {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this)
                .result()
                .orElseThrow(() -> new RuntimeException("Failed to encode BackpackData"));
    }

    public static BackpackData fromNbt(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .result()
                .orElseThrow(() -> new RuntimeException("Failed to decode BackpackData"));
    }
}