package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MoveCToBackpackC2SPacket(MoveMode mode) implements CustomPacketPayload {
    public static final Type<MoveCToBackpackC2SPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "movec_to_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveCToBackpackC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(MoveMode::fromId, MoveMode::getId),
                    MoveCToBackpackC2SPacket::mode,
                    MoveCToBackpackC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}