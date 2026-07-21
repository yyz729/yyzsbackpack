package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SortRequestC2SPacket(int algorithmId, int targetMask)
        implements CustomPacketPayload {

    public static final Type<SortRequestC2SPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "sort_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SortRequestC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SortRequestC2SPacket::algorithmId,
                    ByteBufCodecs.VAR_INT, SortRequestC2SPacket::targetMask,
                    SortRequestC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}