package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.*;
import com.yyz.yyzsbackpack.network.control.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
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

		ModItems.register();

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"), new BackpackDataLoader.ReloadListener());
		PayloadTypeRegistry.serverboundPlay().register(SwitchBackpackC2SPacket.ID, SwitchBackpackC2SPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BackpackDataSyncS2CPacket.ID, BackpackDataSyncS2CPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveIToBackpackC2SPacket.ID, MoveIToBackpackC2SPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveBToInventoryC2SPacket.ID, MoveBToInventoryC2SPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveCToInventoryC2SPacket.ID, MoveCToInventoryC2SPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveIToContainerC2SPacket.ID, MoveIToContainerC2SPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveCToBackpackC2SPacket.ID, MoveCToBackpackC2SPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MoveBToContainerC2SPacket.ID, MoveBToContainerC2SPacket.CODEC);

		PayloadTypeRegistry.serverboundPlay().register(SortRequestC2SPacket.ID, SortRequestC2SPacket.CODEC);

		ServerPacketHandler.register();
	}

	public static BackpackConfig getConfig() {
		return config;
	}

}