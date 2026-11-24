package com.yyz.yyzsbackpack.network.packets.data;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PreferBackpackConfigC2SPacket(boolean prefer) implements CustomPacketPayload {
    public static final Type<PreferBackpackConfigC2SPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "prefer_backpack_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PreferBackpackConfigC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    PreferBackpackConfigC2SPacket::prefer,
                    PreferBackpackConfigC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}