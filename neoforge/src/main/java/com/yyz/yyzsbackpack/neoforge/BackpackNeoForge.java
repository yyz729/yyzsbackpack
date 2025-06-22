package com.yyz.yyzsbackpack.neoforge;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;

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
    public static ItemStack getEquipped(Player player) {
        if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
            // 直接使用 Optional 替代 resolve()
            Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);
            if (curiosHandler.isPresent()) {
                Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                        stack -> stack.getItem() instanceof BackpackItem
                );
                if (backpackSlot.isPresent()) {
                    return backpackSlot.get().stack();
                }
            }
        }
        return player.getInventory().getItem(36);
    }

    public static Container getContainer(Player player) {
        if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
            // 直接使用 Optional 替代 resolve()
            Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);
            if (curiosHandler.isPresent()) {
                Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                        stack -> stack.getItem() instanceof BackpackItem
                );
                if (backpackSlot.isPresent()) {
                    SlotContext slotContext = backpackSlot.get().slotContext();
                    String slotId = slotContext.identifier();
                    int slotIndex = slotContext.index();

                    Optional<ICurioStacksHandler> stacksHandler = curiosHandler.get().getStacksHandler(slotId);
                    if (stacksHandler.isPresent()) {
                        return new CuriosContainerAdapter(stacksHandler.get().getStacks(), slotIndex);
                    }
                }
            }
        }
        return player.getInventory();
    }

    public static int getIndex(Player player) {
        if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
            // 直接使用 Optional 替代 resolve()
            Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);
            if (curiosHandler.isPresent()) {
                Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                        stack -> stack.getItem() instanceof BackpackItem
                );
                if (backpackSlot.isPresent()) {
                    Container container = getContainer(player);
                    if (container instanceof CuriosContainerAdapter) {
                        return ((CuriosContainerAdapter) container).getBackpackSlotIndex();
                    }
                }
            }
        }
        return 36;
    }
}
