package com.yyz.yyzsbackpack.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record BackpackIdComponent(UUID id) {
    public static final Codec<BackpackIdComponent> CODEC =
            Codec.STRING.xmap(UUID::fromString, UUID::toString)
                    .xmap(BackpackIdComponent::new, BackpackIdComponent::id);

    public static final StreamCodec<ByteBuf, BackpackIdComponent> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC);
}