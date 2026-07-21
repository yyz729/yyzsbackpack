package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Backpack.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Backpack.MOD_ID, value = Dist.CLIENT)
public class BackpackClient {
    public BackpackClient(ModContainer container) {

    }

    @SubscribeEvent
    public static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "client_backpack_data"),new BackpackDataLoaderClient.ReloadListener());
    }
}
