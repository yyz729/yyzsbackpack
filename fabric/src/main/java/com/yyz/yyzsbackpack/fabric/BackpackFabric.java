package com.yyz.yyzsbackpack.fabric;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import static com.yyz.yyzsbackpack.Backpack.MOD_ID;

public final class BackpackFabric implements ModInitializer {
    public static final BackpackItem WOOLEN_BACKPACK = new BackpackItem(BackpackMaterial.WOOLEN, new Item.Properties().stacksTo(1));
    public static final BackpackItem STONE_BACKPACK = new BackpackItem(BackpackMaterial.STONE, new Item.Properties().stacksTo(1));
    public static final BackpackItem IRON_BACKPACK = new BackpackItem(BackpackMaterial.IRON, new Item.Properties().stacksTo(1));
    public static final BackpackItem GOLD_BACKPACK = new BackpackItem(BackpackMaterial.GOLD, new Item.Properties().stacksTo(1));
    public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem(BackpackMaterial.DIAMOND, new Item.Properties().stacksTo(1));
    public static final BackpackItem NETHERITE_BACKPACK = new BackpackItem(BackpackMaterial.NETHERITE, new Item.Properties().stacksTo(1).fireResistant());

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

    public static void register(){
        // 注册所有背包物品
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "woolen_backpack"), WOOLEN_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "stone_backpack"), STONE_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "iron_backpack"), IRON_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "group"), GROUP);
    }

    @Override
    public void onInitialize() {
        register();
        Backpack.init();
    }
}
