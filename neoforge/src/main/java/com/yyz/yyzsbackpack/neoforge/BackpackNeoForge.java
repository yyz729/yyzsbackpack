package com.yyz.yyzsbackpack.neoforge;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

@Mod(Backpack.MOD_ID)
public final class BackpackNeoForge {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Backpack.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Backpack.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Backpack.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BACKPACK_TAB = TABS.register("backpack_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BackpackNeoForge.GOLD_BACKPACK.get()))
                    .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                    .displayItems((context, entries) -> {
                        // 按材质等级从低到高排序
                        entries.accept(BackpackNeoForge.WOOLEN_BACKPACK.get());
                        entries.accept(BackpackNeoForge.STONE_BACKPACK.get());
                        entries.accept(BackpackNeoForge.IRON_BACKPACK.get());
                        entries.accept(BackpackNeoForge.GOLD_BACKPACK.get());
                        entries.accept(BackpackNeoForge.DIAMOND_BACKPACK.get());
                        entries.accept(BackpackNeoForge.NETHERITE_BACKPACK.get());
                    })
                    .build());

    // 背包物品注册
    public static final DeferredHolder<Item, Item> NETHERITE_BACKPACK = ITEMS.register("netherite_backpack",
            () -> new BackpackItem(BackpackMaterial.NETHERITE, new Item.Properties().stacksTo(1).fireResistant()));

    public static final DeferredHolder<Item, Item> DIAMOND_BACKPACK = ITEMS.register("diamond_backpack",
            () -> new BackpackItem(BackpackMaterial.DIAMOND, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> GOLD_BACKPACK = ITEMS.register("gold_backpack",
            () -> new BackpackItem(BackpackMaterial.GOLD, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> IRON_BACKPACK = ITEMS.register("iron_backpack",
            () -> new BackpackItem(BackpackMaterial.IRON, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> STONE_BACKPACK = ITEMS.register("stone_backpack",
            () -> new BackpackItem(BackpackMaterial.STONE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> WOOLEN_BACKPACK = ITEMS.register("woolen_backpack",
            () -> new BackpackItem(BackpackMaterial.WOOLEN, new Item.Properties().stacksTo(1)));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> BACKPACK_ITEMS_COMPONENT =
            DATA_COMPONENTS.register("backpack_items", () ->
                    DataComponentType.<List<ItemStack>>builder()
                            .persistent(Codec.list(ItemStack.OPTIONAL_CODEC)) // 注意使用 ItemStack.CODEC
                            .build()
            );


    public BackpackNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        Backpack.init();
    }
}
