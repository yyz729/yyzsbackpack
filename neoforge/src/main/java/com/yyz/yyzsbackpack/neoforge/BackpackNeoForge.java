package com.yyz.yyzsbackpack.neoforge;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

@Mod(Backpack.MOD_ID)
public final class BackpackNeoForge {
    public BackpackNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        DATA_COMPONENT.register(modEventBus);
        Backpack.init();
    }



    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Backpack.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Backpack.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENT = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Backpack.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BACKPACK_TAB = TABS.register("backpack_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BackpackNeoForge.GOLD_BACKPACK.get()))
                    .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                    .displayItems((context, entries) -> {
                        entries.accept(BackpackNeoForge.IRON_BACKPACK.get());
                        entries.accept(BackpackNeoForge.GOLD_BACKPACK.get());
                        entries.accept(BackpackNeoForge.DIAMOND_BACKPACK.get());
                        entries.accept(BackpackNeoForge.NETHERITE_BACKPACK.get());
                    })
                    .build());

    // 背包物品注册（使用DeferredItem）
    public static final DeferredItem<Item> NETHERITE_BACKPACK = ITEMS.registerItem(
            "netherite_backpack",
            props -> new BackpackItem("netherite", props),
            new Item.Properties().stacksTo(1).fireResistant()
    );

    public static final DeferredItem<Item> DIAMOND_BACKPACK = ITEMS.registerItem(
            "diamond_backpack",
            props -> new BackpackItem("diamond", props),
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<Item> GOLD_BACKPACK = ITEMS.registerItem(
            "gold_backpack",
            props -> new BackpackItem("gold", props),
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<Item> IRON_BACKPACK = ITEMS.registerItem(
            "iron_backpack",
            props -> new BackpackItem("iron", props),
            new Item.Properties().stacksTo(1)
    );


    // 数据组件注册（使用Supplier）
    public static final Supplier<DataComponentType<List<ItemStack>>> BACKPACK_ITEMS_COMPONENT = DATA_COMPONENT.registerComponentType(
            "backpack_items",
            builder -> builder.persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
    );

    public static ItemStack getEquipped(Player player) {
        return player.getInventory().getItem(36+ BackpackHelper.getMaxBackpackSize());
    }

    public static ItemStack getEquippedL(LivingEntity player) {
        return player.getSlot(36+ BackpackHelper.getMaxBackpackSize()).get();
    }

    public static Container getContainer(Player player) {
        return player.getInventory();
    }

    public static int getIndex(Player player) {
        return 36+BackpackHelper.getMaxBackpackSize();
    }
}
