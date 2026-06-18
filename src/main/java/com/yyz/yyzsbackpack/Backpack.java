package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Backpack implements ModInitializer {
	public static final String MOD_ID = "yyzsbackpack";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static BackpackItem IRON_BACKPACK;
	public static BackpackItem GOLD_BACKPACK ;
	public static BackpackItem DIAMOND_BACKPACK;
	public static BackpackItem NETHERITE_BACKPACK;

	public static final CreativeModeTab GROUP = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(GOLD_BACKPACK))
			.title(Component.translatable("itemGroup.yyzsbackpack.title"))
			.displayItems((context, entries) -> {

				entries.accept(IRON_BACKPACK);
				entries.accept(GOLD_BACKPACK);
				entries.accept(DIAMOND_BACKPACK);
				entries.accept(NETHERITE_BACKPACK);

			})
			.build();

	@Override
	public void onInitialize() {

		IRON_BACKPACK = new BackpackItem("iron", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "iron_backpack"))));
		GOLD_BACKPACK = new BackpackItem("gold", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "gold_backpack"))));
		DIAMOND_BACKPACK = new BackpackItem("diamond", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "diamond_backpack"))));
		NETHERITE_BACKPACK = new BackpackItem("netherite", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "netherite_backpack"))).fireResistant());

		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "iron_backpack"), IRON_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "group"), GROUP);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"), new BackpackDataLoader.ReloadListener());
	}
}