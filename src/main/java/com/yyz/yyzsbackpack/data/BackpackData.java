package com.yyz.yyzsbackpack.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import net.minecraft.resources.Identifier;

import java.util.List;

public record BackpackData(
    String type,
    int size,
    Identifier guiTexture,
    int backgroundX,
    int backgroundY,
    List<LayoutSegment> segments
) {
    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(BackpackData::type),
                    Codec.INT.fieldOf("size").forGetter(BackpackData::size),
                    Identifier.CODEC.fieldOf("guiTexture").forGetter(BackpackData::guiTexture),
                    Codec.INT.fieldOf("backgroundX").forGetter(BackpackData::backgroundX),
                    Codec.INT.fieldOf("backgroundY").forGetter(BackpackData::backgroundY),
                    Codec.list(LayoutSegment.CODEC).fieldOf("segments").forGetter(BackpackData::segments)
            ).apply(instance, BackpackData::new)
    );
}

