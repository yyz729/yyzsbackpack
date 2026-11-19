package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.config.BackpackControlConfig;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackOffsetConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.data.ColorDataLoaderClient;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.*;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Backpack implements ModInitializer {
    public static final String MOD_ID = "yyzsbackpack";

    private static BackpackMainConfig mainConfig;
    private static BackpackControlConfig controlConfig;
    private static BackpackUiConfig uiConfig;
    private static BackpackOffsetConfig offsetConfig;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new BackpackDataLoader.ReloadListener(ResourceLocation.fromNamespaceAndPath(MOD_ID, "server_backpack_data")));
        ModItems.register();
        ModEffects.register();
        ModPackets.registerPackets();

        ServerPacketHandler.register();
        ModComponents.register();

        registerItemColors();

        File dir = FabricLoader.getInstance().getConfigDir()
                .resolve("yyzsbackpack").toFile();

        BackpackMainConfig.loadConfig(new File(dir, "yyzsbackpack.json"));
        BackpackControlConfig.loadConfig(new File(dir, "control"));
        BackpackOffsetConfig.loadConfig(new File(dir, "offset"));
        BackpackUiConfig.loadConfig(new File(dir, "ui"));
    }

    public static BackpackMainConfig getMainConfig() {
        return BackpackMainConfig.getInstance();
    }

    public static void registerItemColors() {
        List<Item> backpacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BackpackItem) {
                backpacks.add(item);
            }
        }

        if (backpacks.isEmpty()) {
            return;
        }

        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return DyedItemColor.getOrDefault(
                                stack,
                                ColorDataLoaderClient.getDefaultColor(stack.getItem())
                        );
                    }
                    return -1;
                },
                backpacks.toArray(Item[]::new)
        );
    }
}