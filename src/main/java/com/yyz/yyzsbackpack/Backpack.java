package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


import java.io.File;
import java.util.Map;

@Mod(Backpack.MOD_ID)
public class Backpack {
    public static final String MOD_ID = "yyzsbackpack";
    private static BackpackConfig config;

    public static final Logger LOGGER = LogUtils.getLogger();

    public Backpack(IEventBus modEventBus, ModContainer modContainer) {

        config = BackpackConfig.loadConfig(new File(FMLPaths.CONFIGDIR.get() + "/yyzsbackpack.json"));
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModComponents.DATA_COMPONENTS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        Map<String, BackpackData> allData = BackpackDataLoader.getAllData();

        // 发送给该玩家
        PacketDistributor.sendToPlayer(player, new BackpackDataSyncS2CPacket(allData));

        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
        if (player instanceof IBackpackData data) {
            data.yyzsbackpack$setSyncedBackpack(selected);
        }
    }

    @SubscribeEvent
    private void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"),new BackpackDataLoader.ReloadListener());
    }

    public static BackpackConfig getConfig() {
        return config;
    }
}