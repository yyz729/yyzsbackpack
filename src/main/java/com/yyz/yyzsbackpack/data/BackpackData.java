package com.yyz.yyzsbackpack.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

public record BackpackData(
    String type,
    int size,
    Identifier guiTexture,
    int backgroundX,
    int backgroundY,
    boolean forceServer,
    int maxVisibleTabs,
    List<LayoutSegment> segments
) {
    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(BackpackData::type),
                    Codec.INT.optionalFieldOf("size", 0).forGetter(BackpackData::size),
                    Identifier.CODEC.fieldOf("guiTexture").forGetter(BackpackData::guiTexture),
                    Codec.INT.fieldOf("backgroundX").forGetter(BackpackData::backgroundX),
                    Codec.INT.fieldOf("backgroundY").forGetter(BackpackData::backgroundY),
                    Codec.BOOL.optionalFieldOf("force_server", false).forGetter(BackpackData::forceServer),
                    Codec.INT.optionalFieldOf("maxVisibleTabs", -1).forGetter(BackpackData::maxVisibleTabs),
                    Codec.list(LayoutSegment.CODEC).fieldOf("segments").forGetter(BackpackData::segments)
            ).apply(instance, BackpackData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BackpackData decode(RegistryFriendlyByteBuf buf) {
                    CompoundTag tag = buf.readNbt();
                    return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BackpackData data) {
                    CompoundTag tag = (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
                    buf.writeNbt(tag);
                }
            };
}

