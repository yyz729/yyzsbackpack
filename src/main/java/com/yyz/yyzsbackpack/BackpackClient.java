package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.network.BackpackDataSyncS2CPacket;
import com.yyz.yyzsbackpack.network.ClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BackpackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
    }
}