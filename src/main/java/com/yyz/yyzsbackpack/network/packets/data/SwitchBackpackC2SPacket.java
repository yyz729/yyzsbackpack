package com.yyz.yyzsbackpack.network.packets.data;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SwitchBackpackC2SPacket(int index) implements CustomPacketPayload {
    public static final Type<SwitchBackpackC2SPacket> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "switch_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchBackpackC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SwitchBackpackC2SPacket::index,
                    SwitchBackpackC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}