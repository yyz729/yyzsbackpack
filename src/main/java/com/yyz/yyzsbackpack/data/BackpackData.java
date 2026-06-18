package com.yyz.yyzsbackpack.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record BackpackData(
    String type,
    int size,
    int columns,
    int rows,
    int guiWidth,
    int guiHeight,
    Identifier guiTexture
) {
    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(BackpackData::type),
                    Codec.INT.fieldOf("size").forGetter(BackpackData::size),
                    Codec.INT.fieldOf("columns").forGetter(BackpackData::columns),
                    Codec.INT.fieldOf("rows").forGetter(BackpackData::rows),
                    Codec.INT.fieldOf("guiWidth").forGetter(BackpackData::guiWidth),
                    Codec.INT.fieldOf("guiHeight").forGetter(BackpackData::guiHeight),
                    Identifier.CODEC.fieldOf("guiTexture").forGetter(BackpackData::guiTexture)
            ).apply(instance, BackpackData::new)
    );
}