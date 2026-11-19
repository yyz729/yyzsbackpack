package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.client.gui.screen.BackpackConfigScreen;
import com.yyz.yyzsbackpack.client.key.BackpackKeyBinding;
import com.yyz.yyzsbackpack.data.ColorDataLoaderClient;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;

@Mod(value = Backpack.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Backpack.MOD_ID, value = Dist.CLIENT)
public class BackpackClient {
    public BackpackClient(ModContainer container) {

    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new BackpackDataLoaderClient.ReloadListener(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "client_backpack_data")));
        event.registerReloadListener(new ColorDataLoaderClient.ReloadListener(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "client_color_data")));
    }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        List<Item> backpacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BackpackItem) {
                backpacks.add(item);
            }
        }
        if (backpacks.isEmpty()) {
            return;
        }
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0) {
                return DyedItemColor.getOrDefault(stack, ColorDataLoaderClient.getDefaultColor(stack.getItem()));
            }
            return -1;
        }, backpacks.toArray(Item[]::new));
    }



    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(BackpackKeyBinding.KEY_SORT);
        event.register(BackpackKeyBinding.KEY_OPEN);
        event.register(BackpackKeyBinding.KEY_CONFIG);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (BackpackKeyBinding.KEY_CONFIG.consumeClick()) {
            if (client.screen == null) {
                BackpackConfigScreen.open(null);
            } else {
                BackpackConfigScreen.open(client.screen);
            }
        }
    }
}
