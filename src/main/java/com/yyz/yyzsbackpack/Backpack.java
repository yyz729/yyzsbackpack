package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.*;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.component.DyedItemColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Backpack implements ModInitializer {
    public static final String MOD_ID = "yyzsbackpack";
    private static BackpackConfig config;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        config = BackpackConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir() + "/yyzsbackpack.json"));
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new BackpackDataLoader.ReloadListener(ResourceLocation.fromNamespaceAndPath(MOD_ID, "server_backpack_data")));
        ModItems.register();
        ModEffects.register();
        ModPackets.registerPackets();

        ServerPacketHandler.register();
        ModComponents.register();

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return DyedItemColor.getOrDefault(stack, -6265536);
                    }
                    return -1;
                }, ModItems.IRON_BACKPACK, ModItems.GOLD_BACKPACK,
                ModItems.DIAMOND_BACKPACK, ModItems.NETHERITE_BACKPACK);
    }


    public static BackpackConfig getConfig() {
        return config;
    }

}