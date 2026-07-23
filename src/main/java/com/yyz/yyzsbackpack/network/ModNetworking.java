package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import com.yyz.yyzsbackpack.network.packets.control.*;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Backpack.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        // S2C 包
        CHANNEL.messageBuilder(BackpackDataSyncS2CPacket.class, id++)
                .encoder((packet, buf) -> BackpackDataSyncS2CPacket.write(buf, packet))
                .decoder(BackpackDataSyncS2CPacket::read)
                .consumerMainThread(ClientPacketHandler::handleBackpackDataSync)
                .add();

        // C2S 包
        CHANNEL.messageBuilder(SwitchBackpackC2SPacket.class, id++)
                .encoder((packet, buf) -> SwitchBackpackC2SPacket.write(buf, packet))
                .decoder(SwitchBackpackC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleSwitchBackpack)
                .add();

        CHANNEL.messageBuilder(MoveIToBackpackC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveIToBackpackC2SPacket.write(buf, packet))
                .decoder(MoveIToBackpackC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveIToBackpack)
                .add();

        CHANNEL.messageBuilder(MoveBToInventoryC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveBToInventoryC2SPacket.write(buf, packet))
                .decoder(MoveBToInventoryC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveBToInventory)
                .add();

        CHANNEL.messageBuilder(MoveCToInventoryC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveCToInventoryC2SPacket.write(buf, packet))
                .decoder(MoveCToInventoryC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveCToInventory)
                .add();

        CHANNEL.messageBuilder(MoveIToContainerC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveIToContainerC2SPacket.write(buf, packet))
                .decoder(MoveIToContainerC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveIToContainer)
                .add();

        CHANNEL.messageBuilder(MoveCToBackpackC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveCToBackpackC2SPacket.write(buf, packet))
                .decoder(MoveCToBackpackC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveCToBackpack)
                .add();

        CHANNEL.messageBuilder(MoveBToContainerC2SPacket.class, id++)
                .encoder((packet, buf) -> MoveBToContainerC2SPacket.write(buf, packet))
                .decoder(MoveBToContainerC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleMoveBToContainer)
                .add();

        CHANNEL.messageBuilder(SortRequestC2SPacket.class, id++)
                .encoder((packet, buf) -> SortRequestC2SPacket.write(buf, packet))
                .decoder(SortRequestC2SPacket::read)
                .consumerMainThread(ServerPacketHandler::handleSortRequest)
                .add();
    }
}