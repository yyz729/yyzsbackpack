package com.yyz.yyzsbackpack;

import com.mojang.logging.LogUtils;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.config.BackpackControlConfig;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.ModNetworking;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

@Mod(Backpack.MOD_ID)
public class Backpack {
    public static final String MOD_ID = "yyzsbackpack";
    private static BackpackMainConfig mainConfig;
    private static BackpackControlConfig controlConfig;
    private static BackpackUiConfig uiConfig;

    public static final Logger LOGGER = LogUtils.getLogger();

    public Backpack() {
        // 获取 mod 事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册所有 DeferredRegister
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);

        // 注册通用事件（玩家登录、重载监听等）
        MinecraftForge.EVENT_BUS.register(this);

        // 注册网络包（在 commonSetup 中执行）
        modEventBus.addListener(this::commonSetup);

        // 加载配置
        File mainfile = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/yyzsbackpack.json").toFile();
        mainConfig = BackpackMainConfig.loadConfig(mainfile);
        File controlDir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/control").toFile();
        controlConfig = BackpackControlConfig.loadConfig(controlDir);
        File uiDir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/ui").toFile();
        uiConfig = BackpackUiConfig.loadConfig(uiDir);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    // 玩家登录时同步背包数据
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Map<String, BackpackData> allData = BackpackDataLoader.getAllData();
            ModNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new BackpackDataSyncS2CPacket(allData)
            );

            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            if (player instanceof IBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
            }
        }
    }

    // 添加数据包重载监听器
    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BackpackDataLoader.ReloadListener(
                new ResourceLocation(MOD_ID, "backpack_data")
        ));
    }

    public static BackpackMainConfig getMainConfig() {
        return mainConfig;
    }

    public static BackpackControlConfig getControlConfig() {
        return controlConfig;
    }
    public static BackpackUiConfig getUiConfig() {
        return uiConfig;
    }
}