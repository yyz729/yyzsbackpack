package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MoveBToInventoryC2SPacket(boolean all) implements CustomPacketPayload {
    public static final Type<MoveBToInventoryC2SPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "moveb_to_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveBToInventoryC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, MoveBToInventoryC2SPacket::all,
                    MoveBToInventoryC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}