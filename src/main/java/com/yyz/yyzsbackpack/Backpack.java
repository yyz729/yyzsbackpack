package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.*;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.recipebook.ServerPlaceRecipe;
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
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"), new BackpackDataLoader.ReloadListener());

		ModItems.register();
		ModEffects.register();
		ModPackets.registerPackets();

		ServerPacketHandler.register();
		ModComponents.register();

	}

	public static BackpackConfig getConfig() {
		return config;
	}

}