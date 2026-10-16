package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record MoveBToInventoryC2SPacket(MoveMode mode) implements CustomPacketPayload {
    public static final Type<MoveBToInventoryC2SPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "moveb_to_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveBToInventoryC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(MoveMode::fromId, MoveMode::getId),
                    MoveBToInventoryC2SPacket::mode,
                    MoveBToInventoryC2SPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}