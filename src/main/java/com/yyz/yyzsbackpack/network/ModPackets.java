package com.yyz.yyzsbackpack.network;

public class ModPackets {

    public static void registerPackets() {
        registerC2SPacket();
        registerS2CPacket();
    }

    private static void registerC2SPacket() {
//        PayloadTypeRegistry.playC2S().register(SwitchBackpackC2SPacket.ID, SwitchBackpackC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveIToBackpackC2SPacket.ID, MoveIToBackpackC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveBToInventoryC2SPacket.ID, MoveBToInventoryC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveCToInventoryC2SPacket.ID, MoveCToInventoryC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveIToContainerC2SPacket.ID, MoveIToContainerC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveCToBackpackC2SPacket.ID, MoveCToBackpackC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(MoveBToContainerC2SPacket.ID, MoveBToContainerC2SPacket.CODEC);
//        PayloadTypeRegistry.playC2S().register(SortRequestC2SPacket.ID, SortRequestC2SPacket.CODEC);
    }

    public static void registerS2CPacket() {
//        PayloadTypeRegistry.playS2C().register(BackpackDataSyncS2CPacket.ID, BackpackDataSyncS2CPacket.CODEC);
    }
}
