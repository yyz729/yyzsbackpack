package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.client.key.BackpackKeyBinding;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Backpack.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BackpackClient {

    @SubscribeEvent
    public static void registerClientReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BackpackDataLoaderClient.ReloadListener(
                new ResourceLocation(Backpack.MOD_ID, "client_backpack_data")
        ));
    }
}