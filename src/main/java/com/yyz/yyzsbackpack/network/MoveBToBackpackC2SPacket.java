package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record MoveBToBackpackC2SPacket(boolean all) implements CustomPacketPayload {
    public static final Type<MoveBToBackpackC2SPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "moveb_to_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveBToBackpackC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, MoveBToBackpackC2SPacket::all,
                    MoveBToBackpackC2SPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}