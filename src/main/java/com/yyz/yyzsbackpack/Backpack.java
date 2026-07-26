package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.config.BackpackControlConfig;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.*;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Backpack implements ModInitializer {
	public static final String MOD_ID = "yyzsbackpack";
	private static BackpackMainConfig mainConfig;
	private static BackpackControlConfig controlConfig;
	private static BackpackUiConfig uiConfig;


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"), new BackpackDataLoader.ReloadListener());

		ModItems.register();
		ModEffects.register();
		ModPackets.registerPackets();

		ServerPacketHandler.register();
		ModComponents.register();

		File mainfile = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/yyzsbackpack.json").toFile();
		mainConfig = BackpackMainConfig.loadConfig(mainfile);
		File controlDir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/control").toFile();
		controlConfig = BackpackControlConfig.loadConfig(controlDir);
		File uiDir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/ui").toFile();
		uiConfig = BackpackUiConfig.loadConfig(uiDir);
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