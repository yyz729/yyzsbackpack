package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;

public class ClientPacketHandler {
    public static  void register() {
        // 客户端接收服务端同步的 BackpackData
        ClientPlayNetworking.registerGlobalReceiver(BackpackDataSyncS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                Map<String, BackpackData> merged = BackpackDataLoaderClient.merge(packet.dataMap());
                BackpackDataLoader.setData(merged);
                Backpack.LOGGER.info("Synced backpack data from server ({} entries after merge)", merged.size());
            });
        });
    }
}
