package com.yyz.yyzsbackpack.network.handler;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;

public class ClientPacketHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                BackpackDataSyncS2CPacket.ID,
                (client, handler, buf, responseSender) -> {
                    BackpackDataSyncS2CPacket packet = BackpackDataSyncS2CPacket.read(buf);
                    client.execute(() -> {
                        Map<String, BackpackData> merged = BackpackDataLoaderClient.merge(packet.dataMap());
                        BackpackDataLoader.setData(merged);
                        Backpack.LOGGER.info("Synced backpack data from server ({} entries after merge)", merged.size());
                    });
                }
        );
    }
}