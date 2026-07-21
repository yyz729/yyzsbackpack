package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Backpack.MOD_ID,value = Dist.CLIENT)
public class ModClientPackets {
    @SubscribeEvent
    public static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
                BackpackDataSyncS2CPacket.ID,
                ClientPacketHandler::handleBackpackDataSync
        );
    }
}
