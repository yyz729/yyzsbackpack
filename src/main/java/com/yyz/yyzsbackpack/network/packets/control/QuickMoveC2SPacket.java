package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record QuickMoveC2SPacket(int slotIndex)
        implements CustomPacketPayload {

    public static final Type<QuickMoveC2SPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "quick_move"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuickMoveC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, QuickMoveC2SPacket::slotIndex,
                    QuickMoveC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}