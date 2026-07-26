package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.config.BackpackControlConfig;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.handler.ServerPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.DyeableLeatherItem;
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


		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new BackpackDataLoader.ReloadListener(new ResourceLocation(MOD_ID, "server_backpack_data")));
		ModItems.register();
		ModEffects.register();

		ServerPacketHandler.register();

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
					if (tintIndex == 0 && stack.getItem() instanceof DyeableLeatherItem dyeable) {
						return dyeable.hasCustomColor(stack) ? dyeable.getColor(stack) : -6265536;
					}
					return -1;
				}, ModItems.IRON_BACKPACK, ModItems.GOLD_BACKPACK,
				ModItems.DIAMOND_BACKPACK, ModItems.NETHERITE_BACKPACK);

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