package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public record BackpackDataSyncS2CPacket(Map<String, BackpackData> dataMap) implements CustomPacketPayload {
    public static final Type<BackpackDataSyncS2CPacket> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "backpack_data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackDataSyncS2CPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, BackpackData.STREAM_CODEC),
                    BackpackDataSyncS2CPacket::dataMap,
                    BackpackDataSyncS2CPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}