package com.yyz.yyzsbackpack.item;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {

    public static final BackpackItem IRON_BACKPACK;
    public static final BackpackItem GOLD_BACKPACK;
    public static final BackpackItem DIAMOND_BACKPACK;
    public static final BackpackItem NETHERITE_BACKPACK;

    public static final CreativeModeTab BACKPACK_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.GOLD_BACKPACK))
            .title(Component.translatable("itemGroup.yyzsbackpack.title"))
            .displayItems((context, entries) -> {
                entries.accept(ModItems.IRON_BACKPACK);
                entries.accept(ModItems.GOLD_BACKPACK);
                entries.accept(ModItems.DIAMOND_BACKPACK);
                entries.accept(ModItems.NETHERITE_BACKPACK);
            })
            .build();



    static {
        IRON_BACKPACK = register("iron_backpack",
                new BackpackItem("iron", createBackpackProperties("iron_backpack", false)));

        GOLD_BACKPACK = register("gold_backpack",
                new BackpackItem("gold", createBackpackProperties("gold_backpack", false)));

        DIAMOND_BACKPACK = register("diamond_backpack",
                new BackpackItem("diamond", createBackpackProperties("diamond_backpack", false)));

        NETHERITE_BACKPACK = register("netherite_backpack",
                new BackpackItem("netherite", createBackpackProperties("netherite_backpack", true)));

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "group"), ModItems.BACKPACK_TAB);

    }

    private static Item.Properties createBackpackProperties(String path, boolean fireResistant) {
        var props = new Item.Properties()
                .stacksTo(1)
                .equippable(EquipmentSlot.CHEST)
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Backpack.MOD_ID, path)));
        if (fireResistant) {
            props.fireResistant();
        }
        return props;
    }

    private static <T extends Item> T register(String path, T item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(Backpack.MOD_ID, path),
                item
        );
    }

    public static void register() {
        // 空方法，仅触发类加载
    }
}