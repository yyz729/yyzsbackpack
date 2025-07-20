package com.yyz.yyzsbackpack.neoforge;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.compat.AccessoriesContainerAdapter;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import com.yyz.yyzsbackpack.neoforge.compat.curios.CuriosContainerAdapter;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
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
import top.theillusivec4.curios.common.CuriosConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(Backpack.MOD_ID)
public final class BackpackNeoForge {

    public static final DeferredRegister.DataComponents DATA_COMPONENT = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,Backpack.MOD_ID);

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
            () -> new BackpackItem(BackpackMaterial.WOODEN, new Item.Properties().stacksTo(1)));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> BACKPACK_ITEMS_COMPONENT = DATA_COMPONENT.registerComponentType(
            "backpack_items",
            builder -> builder
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
    );


    public BackpackNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        DATA_COMPONENT.register(modEventBus);
        Backpack.init();
    }

    public static ItemStack getEquipped(Player player) {
        if(!Backpack.getConfig().force_slot) {
            if (BackpackHelper.isModLoaded("curios")) {
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

            if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios")) {
                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    Map<String, AccessoriesContainer> containers = capability.getContainers();
                    for (AccessoriesContainer container : containers.values()) {
                        Container accessoriesContainer = container.getAccessories();
                        for (int i = 0; i < accessoriesContainer.getContainerSize(); i++) {
                            ItemStack stack = accessoriesContainer.getItem(i);
                            if (stack.getItem() instanceof BackpackItem) {
                                return stack;
                            }
                        }
                    }
                }
            }
        }
        return player.getInventory().getItem(36+54);
    }

    public static Container getContainer(Player player) {
        if(!Backpack.getConfig().force_slot) {
            if (BackpackHelper.isModLoaded("curios")) {
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

            if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios")) {
                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    Map<String, AccessoriesContainer> containers = capability.getContainers();
                    for (AccessoriesContainer container : containers.values()) {
                        Container accessoriesContainer = container.getAccessories();
                        for (int i = 0; i < accessoriesContainer.getContainerSize(); i++) {
                            ItemStack stack = accessoriesContainer.getItem(i);
                            if (stack.getItem() instanceof BackpackItem) {
                                return new AccessoriesContainerAdapter(accessoriesContainer, i);
                            }
                        }
                    }
                }
            }
        }
        return player.getInventory();
    }

    public static int getIndex(Player player) {
        if(!Backpack.getConfig().force_slot) {
            if (BackpackHelper.isModLoaded("curios")) {
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

            if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios")) {
                Container container = getContainer(player);
                if (container instanceof AccessoriesContainerAdapter) {
                    return ((AccessoriesContainerAdapter) container).getBackpackSlotIndex();
                }
            }
        }
        return 36+54;
    }

    public static boolean getEmptyRule(Player player) {
        if(BackpackHelper.isModLoaded("curios")) {
            if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && CuriosConfig.SERVER.keepCurios.get() == CuriosConfig.KeepCurios.ON) {
                return false;
            }
        }
        return true;
    }
}
