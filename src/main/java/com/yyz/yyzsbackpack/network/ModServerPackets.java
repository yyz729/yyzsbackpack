package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import com.yyz.yyzsbackpack.network.packets.control.*;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Backpack.MOD_ID)
public class ModServerPackets {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                BackpackDataSyncS2CPacket.ID,
                BackpackDataSyncS2CPacket.CODEC,
                ClientPacketHandler::handleBackpackDataSync
        );
        // 注册所有 C2S 包
        registrar.playToServer(
                SwitchBackpackC2SPacket.ID,
                SwitchBackpackC2SPacket.CODEC,
                ServerPacketHandler::handleSwitchBackpack
        );

        registrar.playToServer(
                MoveIToBackpackC2SPacket.ID,
                MoveIToBackpackC2SPacket.CODEC,
                ServerPacketHandler::handleMoveIToBackpack
        );

        registrar.playToServer(
                MoveBToInventoryC2SPacket.ID,
                MoveBToInventoryC2SPacket.CODEC,
                ServerPacketHandler::handleMoveBToInventory
        );

        registrar.playToServer(
                MoveCToInventoryC2SPacket.ID,
                MoveCToInventoryC2SPacket.CODEC,
                ServerPacketHandler::handleMoveCToInventory
        );

        registrar.playToServer(
                MoveIToContainerC2SPacket.ID,
                MoveIToContainerC2SPacket.CODEC,
                ServerPacketHandler::handleMoveIToContainer
        );

        registrar.playToServer(
                MoveCToBackpackC2SPacket.ID,
                MoveCToBackpackC2SPacket.CODEC,
                ServerPacketHandler::handleMoveCToBackpack
        );

        registrar.playToServer(
                MoveBToContainerC2SPacket.ID,
                MoveBToContainerC2SPacket.CODEC,
                ServerPacketHandler::handleMoveBToContainer
        );

        registrar.playToServer(
                SortRequestC2SPacket.ID,
                SortRequestC2SPacket.CODEC,
                ServerPacketHandler::handleSortRequest
        );
    }
}
