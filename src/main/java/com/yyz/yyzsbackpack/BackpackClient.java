package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.client.key.BackpackKeyBinding;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class BackpackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        BackpackKeyBinding.register();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BackpackDataLoaderClient.ReloadListener(new ResourceLocation(Backpack.MOD_ID, "client_backpack_data")));

    }
}