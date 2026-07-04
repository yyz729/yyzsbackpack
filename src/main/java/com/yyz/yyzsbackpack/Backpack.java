package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.api.BackpackSlotProvider;
import com.yyz.yyzsbackpack.api.BackpackSlotReference;
import com.yyz.yyzsbackpack.api.VanillaBackpackSlotProvider;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

		IRON_BACKPACK = new BackpackItem("iron", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "iron_backpack"))).equippable(EquipmentSlot.CHEST));
		GOLD_BACKPACK = new BackpackItem("gold", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "gold_backpack"))).equippable(EquipmentSlot.CHEST));
		DIAMOND_BACKPACK = new BackpackItem("diamond", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "diamond_backpack"))).equippable(EquipmentSlot.CHEST));
		NETHERITE_BACKPACK = new BackpackItem("netherite", new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "netherite_backpack"))).fireResistant().equippable(EquipmentSlot.CHEST));

		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "iron_backpack"), IRON_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "group"), GROUP);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "backpack_data"), new BackpackDataLoader.ReloadListener());

		PayloadTypeRegistry.serverboundPlay().register(SwitchBackpackC2SPacket.ID, SwitchBackpackC2SPacket.CODEC);
	}


	private static final List<BackpackSlotProvider> SLOT_PROVIDERS = new ArrayList<>();

	static {
		SLOT_PROVIDERS.add(new VanillaBackpackSlotProvider()); // 默认注册
	}

	public static void registerSlotProvider(BackpackSlotProvider provider) {
		SLOT_PROVIDERS.add(provider);
	}

	public static List<BackpackSlotReference> getAllBackpackSlots(Player player) {
		List<BackpackSlotReference> slots = new ArrayList<>();
		for (BackpackSlotProvider provider : SLOT_PROVIDERS) {
			slots.addAll(provider.getSlots(player));
		}

		slots.removeIf(ref -> !(ref.getStack().getItem() instanceof BackpackItem));
		return slots;
	}

	/**
	 * 获取玩家当前选中的背包物品（按持久化索引）。
	 */
	public static ItemStack getSelectedBackpack(Player player) {
		List<BackpackSlotReference> slots = getAllBackpackSlots(player);
		if (slots.isEmpty()) return ItemStack.EMPTY;
		int idx = getSelectedIndex(player);
		if (idx < 0 || idx >= slots.size()) {
			idx = 0;
			setSelectedIndex(player, idx);
		}
		return slots.get(idx).getStack();
	}

	private static final String SELECTED_INDEX_KEY = "BackpackSelectedIndex";


	public static int getSelectedIndex(Player player) {
		CustomData data = player.get(DataComponents.CUSTOM_DATA);
		if (data != null) {
			CompoundTag tag = data.copyTag();
			return tag.getIntOr(SELECTED_INDEX_KEY, 0);
		}
		return 0;
	}

	public static void setSelectedIndex(Player player, int index) {
		CustomData old = player.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = old != null ? old.copyTag() : new CompoundTag();
		tag.putInt(SELECTED_INDEX_KEY, index);
		player.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	/**
	 * 获取玩家所有背包物品列表（仅含有效背包）。
	 */
	public static List<ItemStack> getAllBackpackStacks(Player player) {
		return getAllBackpackSlots(player).stream()
				.map(BackpackSlotReference::getStack)
				.toList();
	}
}