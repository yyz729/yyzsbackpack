package com.yyz.yyzsbackpack.fabric;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Function;

import static com.yyz.yyzsbackpack.Backpack.MOD_ID;

public final class BackpackFabric implements ModInitializer {
    public static final BackpackItem WOOLEN_BACKPACK = (BackpackItem) register("woolen_backpack", props -> new BackpackItem(BackpackMaterial.WOOLEN, props), new Item.Properties().stacksTo(1));
    public static final BackpackItem STONE_BACKPACK = (BackpackItem) register("stone_backpack", props -> new BackpackItem(BackpackMaterial.STONE, props), new Item.Properties().stacksTo(1));
    public static final BackpackItem IRON_BACKPACK = (BackpackItem) register("iron_backpack", props -> new BackpackItem(BackpackMaterial.IRON, props), new Item.Properties().stacksTo(1));
    public static final BackpackItem GOLD_BACKPACK = (BackpackItem) register("gold_backpack", props -> new BackpackItem(BackpackMaterial.GOLD, props), new Item.Properties().stacksTo(1));
    public static final BackpackItem DIAMOND_BACKPACK = (BackpackItem) register("diamond_backpack", props -> new BackpackItem(BackpackMaterial.DIAMOND, props), new Item.Properties().stacksTo(1));
    public static final BackpackItem NETHERITE_BACKPACK = (BackpackItem) register("netherite_backpack", props -> new BackpackItem(BackpackMaterial.NETHERITE, props), new Item.Properties().stacksTo(1).fireResistant());
    public static final CreativeModeTab GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GOLD_BACKPACK))
            .title(Component.translatable("itemGroup.yyzsbackpack.title"))
            .displayItems((context, entries) -> {
                // 按顺序添加所有背包到物品组
                entries.accept(WOOLEN_BACKPACK);
                entries.accept(STONE_BACKPACK);
                entries.accept(IRON_BACKPACK);
                entries.accept(GOLD_BACKPACK);
                entries.accept(DIAMOND_BACKPACK);
                entries.accept(NETHERITE_BACKPACK);
            })
            .build();
    public static final DataComponentType<List<ItemStack>> BACKPACK_ITEMS_COMPONENT =
            DataComponentType.<List<ItemStack>>builder()
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .build();

    public static void register(){

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "backpack_items"),
                BACKPACK_ITEMS_COMPONENT);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "group"), GROUP);
    }

    public static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {
        final ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        return Items.registerItem(registryKey, factory, settings);
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        register();
        Backpack.init();
    }
}
