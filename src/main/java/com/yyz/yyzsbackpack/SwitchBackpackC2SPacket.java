package com.yyz.yyzsbackpack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SwitchBackpackC2SPacket(int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SwitchBackpackC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "switch_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchBackpackC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SwitchBackpackC2SPacket::index,
                    SwitchBackpackC2SPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}