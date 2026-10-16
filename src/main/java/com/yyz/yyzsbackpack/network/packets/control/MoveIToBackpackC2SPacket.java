package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record MoveIToBackpackC2SPacket(MoveMode mode) implements CustomPacketPayload {
    public static final Type<MoveIToBackpackC2SPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "movei_to_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveIToBackpackC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(MoveMode::fromId, MoveMode::getId),
                    MoveIToBackpackC2SPacket::mode,
                    MoveIToBackpackC2SPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}