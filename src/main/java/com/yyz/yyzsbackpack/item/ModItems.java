package com.yyz.yyzsbackpack.item;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // ---------- 注册器 ----------
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Backpack.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Backpack.MOD_ID);

    // ---------- 物品 ----------
    public static final DeferredItem<BackpackItem> IRON_BACKPACK = ITEMS.register(
            "iron_backpack",
            () -> new BackpackItem("iron", createBackpackProperties("iron_backpack", false))
    );
    public static final DeferredItem<BackpackItem> GOLD_BACKPACK = ITEMS.register(
            "gold_backpack",
            () -> new BackpackItem("gold", createBackpackProperties("gold_backpack", false))
    );
    public static final DeferredItem<BackpackItem> DIAMOND_BACKPACK = ITEMS.register(
            "diamond_backpack",
            () -> new BackpackItem("diamond", createBackpackProperties("diamond_backpack", false))
    );
    public static final DeferredItem<BackpackItem> NETHERITE_BACKPACK = ITEMS.register(
            "netherite_backpack",
            () -> new BackpackItem("netherite", createBackpackProperties("netherite_backpack", true))
    );

    // ---------- 创造模式标签页 ----------
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BACKPACK_TAB =
            CREATIVE_MODE_TABS.register(
                    "backpack_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> GOLD_BACKPACK.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(IRON_BACKPACK.get());
                                output.accept(GOLD_BACKPACK.get());
                                output.accept(DIAMOND_BACKPACK.get());
                                output.accept(NETHERITE_BACKPACK.get());
                            })
                            .build()
            );



    private static Item.Properties createBackpackProperties(String path, boolean fireResistant) {
        var props = new Item.Properties()
                .stacksTo(1);
        if (fireResistant) {
            props.fireResistant();
        }
        return props;
    }
}