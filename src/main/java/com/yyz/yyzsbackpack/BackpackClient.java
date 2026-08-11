package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.client.key.BackpackKeyBinding;
import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(value = Backpack.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Backpack.MOD_ID, value = Dist.CLIENT)
public class BackpackClient {
    public BackpackClient(ModContainer container) {

    }

    @SubscribeEvent
    public static void registerClientReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BackpackDataLoaderClient.ReloadListener(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "client_backpack_data")));
    }

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return DyedItemColor.getOrDefault(stack, -6265536);
                    }
                    return -1;
                }, ModItems.IRON_BACKPACK, ModItems.GOLD_BACKPACK,
                ModItems.DIAMOND_BACKPACK, ModItems.NETHERITE_BACKPACK);
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(BackpackKeyBinding.KEY_SORT);
        event.register(BackpackKeyBinding.KEY_OPEN);
    }
}
