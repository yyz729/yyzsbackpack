package com.yyz.yyzsbackpack.network.handler;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class ClientPacketHandler {

    public static void handleBackpackDataSync(BackpackDataSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Map<String, BackpackData> merged = BackpackDataLoaderClient.merge(packet.dataMap());
            BackpackDataLoader.setData(merged);
            Backpack.LOGGER.info("Synced backpack data from server ({} entries after merge)", merged.size());
        });
    }
}
