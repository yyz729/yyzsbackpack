package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class BackpackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "client_backpack_data"), new BackpackDataLoaderClient.ReloadListener());
    }
}