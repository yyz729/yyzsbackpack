package com.yyz.yyzsbackpack.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.yyz.yyzsbackpack.YyzsBackpack.MOD_ID;

public class ModItems {
    public static final BackpackItem WOOLEN_BACKPACK = new BackpackItem(Backpack.WOOLEN);
    public static final BackpackItem STONE_BACKPACK = new BackpackItem(Backpack.STONE);
    public static final BackpackItem IRON_BACKPACK = new BackpackItem(Backpack.IRON);
    public static final BackpackItem GOLD_BACKPACK = new BackpackItem(Backpack.GOLD);
    public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem(Backpack.DIAMOND);
    public static final BackpackItem NETHERITE_BACKPACK = new BackpackItem(Backpack.NETHERITE);

    public static final ItemGroup GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GOLD_BACKPACK))
            .displayName(Text.translatable("itemGroup.yyzsbackpack.title"))
            .entries((context, entries) -> {
                // 按顺序添加所有背包到物品组
                entries.add(WOOLEN_BACKPACK);
                entries.add(STONE_BACKPACK);
                entries.add(IRON_BACKPACK);
                entries.add(GOLD_BACKPACK);
                entries.add(DIAMOND_BACKPACK);
                entries.add(NETHERITE_BACKPACK);
            })
            .build();

    public static void register(){
        // 注册所有背包物品
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "woolen_backpack"), WOOLEN_BACKPACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "stone_backpack"), STONE_BACKPACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_backpack"), IRON_BACKPACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

        // 注册物品组
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "group"), GROUP);
    }
}
