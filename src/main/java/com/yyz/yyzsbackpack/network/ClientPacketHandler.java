package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandler {
    public static  void register() {
        // 客户端接收服务端同步的 BackpackData
        ClientPlayNetworking.registerGlobalReceiver(BackpackDataSyncS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                BackpackDataLoader.setData(packet.dataMap());
                Backpack.LOGGER.info("Received backpack data from server, count: {}", packet.dataMap().size());
            });
        });
    }
}
