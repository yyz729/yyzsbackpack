package com.yyz.yyzsbackpack.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackupRecord;
import com.yyz.yyzsbackpack.compat.AccessoriesContainerAdapter;
import com.yyz.yyzsbackpack.effect.HeavyEffect;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.neoforge.compat.curios.CuriosContainerAdapter;
import com.yyz.yyzsbackpack.util.BackpackBackup;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.CuriosConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(Backpack.MOD_ID)
public final class BackpackNeoForge {

    public static final DeferredRegister.DataComponents DATA_COMPONENT = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,Backpack.MOD_ID);
    // 注册备份组件（存储 List<BackupRecord>）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BackupRecord>>> BACKUP_RECORDS_COMPONENT =
            DATA_COMPONENT.registerComponentType(
                    "backup_records",
                    builder -> builder
                            .persistent(Codec.list(BackupRecord.CODEC)) // 需要实现 BackupRecord 的 Codec
            );




    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Backpack.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Backpack.MOD_ID);

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

    // 背包物品注册
    public static final DeferredHolder<Item, Item> NETHERITE_BACKPACK = ITEMS.register("netherite_backpack",
            () -> new BackpackItem("netherite", new Item.Properties().stacksTo(1).component(BackpackPlatform.getBackpackItemsComponent(), new ArrayList<>()).fireResistant()));

    public static final DeferredHolder<Item, Item> DIAMOND_BACKPACK = ITEMS.register("diamond_backpack",
            () -> new BackpackItem("diamond", new Item.Properties().stacksTo(1).component(BackpackPlatform.getBackpackItemsComponent(), new ArrayList<>())));

    public static final DeferredHolder<Item, Item> GOLD_BACKPACK = ITEMS.register("gold_backpack",
            () -> new BackpackItem("gold", new Item.Properties().stacksTo(1).component(BackpackPlatform.getBackpackItemsComponent(), new ArrayList<>())));

    public static final DeferredHolder<Item, Item> IRON_BACKPACK = ITEMS.register("iron_backpack",
            () -> new BackpackItem("iron", new Item.Properties().stacksTo(1).component(BackpackPlatform.getBackpackItemsComponent(), new ArrayList<>())));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> BACKPACK_ITEMS_COMPONENT = DATA_COMPONENT.registerComponentType(
            "backpack_items",
            builder -> builder
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
    );


    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Backpack.MOD_ID);

    // 注册负重效果
    public static final Holder<MobEffect> HEAVY_EFFECT = MOB_EFFECTS.register("heavy", HeavyEffect::new);


    public BackpackNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        DATA_COMPONENT.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        modEventBus.register(this);
        Backpack.init();

    }


    @SubscribeEvent
    public void onItemColor(RegisterColorHandlersEvent.Item event) {
        // 注册物品颜色提供器
        event.register((itemStack, i) -> i > 0 ? -1 : DyedItemColor.getOrDefault(itemStack, -6265536), BackpackNeoForge.IRON_BACKPACK.get(),BackpackNeoForge.GOLD_BACKPACK.get(),BackpackNeoForge.DIAMOND_BACKPACK.get(),BackpackNeoForge.NETHERITE_BACKPACK.get());
    }

    public static ItemStack getEquipped(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("curios")) {
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

            if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("curios")) {
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
        return player.getInventory().getItem(36+ BackpackHelper.getMaxBackpackSize());
    }

    public static Container getContainer(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("curios")) {
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

            if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("curios")) {
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

    public static boolean getRender(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("curios")) {
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
                            return stacksHandler.get().getRenders().get(slotIndex);
                        }
                    }
                }
            }
        }
        return Backpack.getConfig().render_backpack_model;
    }

    public static int getIndex(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("curios")) {
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

            if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("curios")) {
                Container container = getContainer(player);
                if (container instanceof AccessoriesContainerAdapter) {
                    return ((AccessoriesContainerAdapter) container).getBackpackSlotIndex();
                }
            }
        }
        return 36+BackpackHelper.getMaxBackpackSize();
    }

    public static boolean getEmptyRule(Player player) {
        if(BackpackPlatform.isModLoaded("curios")) {
            if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && CuriosConfig.SERVER.keepCurios.get() == CuriosConfig.KeepCurios.ON) {
                return false;
            }
        }
        return true;
    }
}
