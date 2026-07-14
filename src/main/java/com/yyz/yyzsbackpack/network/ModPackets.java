package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.network.packets.control.*;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModPackets {

    public static void registerPackets() {
        registerC2SPacket();
        registerS2CPacket();
    }

    private static void registerC2SPacket() {
        PayloadTypeRegistry.serverboundPlay().register(SwitchBackpackC2SPacket.ID, SwitchBackpackC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveIToBackpackC2SPacket.ID, MoveIToBackpackC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveBToInventoryC2SPacket.ID, MoveBToInventoryC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveCToInventoryC2SPacket.ID, MoveCToInventoryC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveIToContainerC2SPacket.ID, MoveIToContainerC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveCToBackpackC2SPacket.ID, MoveCToBackpackC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveBToContainerC2SPacket.ID, MoveBToContainerC2SPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SortRequestC2SPacket.ID, SortRequestC2SPacket.CODEC);
    }

    public static void registerS2CPacket() {
        PayloadTypeRegistry.clientboundPlay().register(BackpackDataSyncS2CPacket.ID, BackpackDataSyncS2CPacket.CODEC);
    }
}
