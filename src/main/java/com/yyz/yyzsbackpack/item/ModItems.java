package com.yyz.yyzsbackpack.item;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    // ---------- 注册器 ----------
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Backpack.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Backpack.MOD_ID);

    // ---------- 物品 ----------
    public static final RegistryObject<BackpackItem> IRON_BACKPACK = ITEMS.register(
            "iron_backpack",
            () -> new BackpackItem("iron", createBackpackProperties("iron_backpack", false))
    );

    public static final RegistryObject<BackpackItem> GOLD_BACKPACK = ITEMS.register(
            "gold_backpack",
            () -> new BackpackItem("gold", createBackpackProperties("gold_backpack", false))
    );

    public static final RegistryObject<BackpackItem> DIAMOND_BACKPACK = ITEMS.register(
            "diamond_backpack",
            () -> new BackpackItem("diamond", createBackpackProperties("diamond_backpack", false))
    );

    public static final RegistryObject<BackpackItem> NETHERITE_BACKPACK = ITEMS.register(
            "netherite_backpack",
            () -> new BackpackItem("netherite", createBackpackProperties("netherite_backpack", true))
    );

    // ---------- 创造模式标签页 ----------
    public static final RegistryObject<CreativeModeTab> BACKPACK_TAB = CREATIVE_MODE_TABS.register(
            "backpack_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                    .icon(() -> new ItemStack(GOLD_BACKPACK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(IRON_BACKPACK.get());
                        output.accept(GOLD_BACKPACK.get());
                        output.accept(DIAMOND_BACKPACK.get());
                        output.accept(NETHERITE_BACKPACK.get());
                    })
                    .build()
    );

    // ---------- 工具方法 ----------
    private static Item.Properties createBackpackProperties(String path, boolean fireResistant) {
        var props = new Item.Properties()
                .stacksTo(1);
        if (fireResistant) {
            props.fireResistant();
        }
        return props;
    }
}