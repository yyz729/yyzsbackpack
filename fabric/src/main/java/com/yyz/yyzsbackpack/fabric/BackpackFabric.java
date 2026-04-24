package com.yyz.yyzsbackpack.fabric;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.compat.AccessoriesContainerAdapter;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;


import java.util.List;
import java.util.Map;

import static com.yyz.yyzsbackpack.Backpack.MOD_ID;

public final class BackpackFabric implements ModInitializer {

    public static final BackpackItem IRON_BACKPACK = new BackpackItem("iron", new Item.Properties().stacksTo(1));
    public static final BackpackItem GOLD_BACKPACK = new BackpackItem("gold", new Item.Properties().stacksTo(1));
    public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem("diamond", new Item.Properties().stacksTo(1));
    public static final BackpackItem NETHERITE_BACKPACK = new BackpackItem("netherite", new Item.Properties().stacksTo(1).fireResistant());



    public static final CreativeModeTab GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GOLD_BACKPACK))
            .title(Component.translatable("itemGroup.yyzsbackpack.title"))
            .displayItems((context, entries) -> {

                entries.accept(IRON_BACKPACK);
                entries.accept(GOLD_BACKPACK);
                entries.accept(DIAMOND_BACKPACK);
                entries.accept(NETHERITE_BACKPACK);

            })
            .build();
    public static final DataComponentType<List<ItemStack>> BACKPACK_ITEMS_COMPONENT =
            DataComponentType.<List<ItemStack>>builder()
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .build();

    public static void register(){

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "iron_backpack"), IRON_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

        //Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "netherites_backpack"), NETHERITE_BACKPACKs);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "backpack_items"),
                BACKPACK_ITEMS_COMPONENT);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "group"), GROUP);
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            Backpack.registerCommands(dispatcher);
        });


    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        register();
        Backpack.init();
    }

    public static ItemStack getEquipped(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot){
            if (BackpackPlatform.isModLoaded("trinkets")) {
                return TrinketsApi.getTrinketComponent(player)
                        .map(trinketComponent -> {
                            // 使用 Predicate 检查是否为 BackpackItem
                            List<Tuple<SlotReference, ItemStack>> list = trinketComponent.getEquipped(
                                    stack -> stack.getItem() instanceof BackpackItem
                            );
                            return !list.isEmpty() ? list.get(0).getB() : ItemStack.EMPTY;
                        })
                        .orElse(ItemStack.EMPTY);
            }

            else if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("trinkets")) {
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
        // 检查背包槽（索引36）
        return player.getInventory().getItem(36+ BackpackHelper.getMaxBackpackSize());
    }

    public static Container getContainer(Player player) {
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("trinkets")) {
                return TrinketsApi.getTrinketComponent(player)
                        .map(trinketComponent -> {
                            // 使用 Predicate 检查 BackpackItem
                            List<Tuple<SlotReference, ItemStack>> list = trinketComponent.getEquipped(
                                    stack -> stack.getItem() instanceof BackpackItem
                            );
                            return !list.isEmpty()
                                    ? list.get(0).getA().inventory()  // 返回背包容器
                                    : player.getInventory();         // 未找到时返回玩家库存
                        })
                        .orElse(player.getInventory());
            }

            else if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("trinkets")) {
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
        if(!Backpack.getConfig().use_dedicated_slot) {
            if (BackpackPlatform.isModLoaded("trinkets")) {
                return TrinketsApi.getTrinketComponent(player)
                        .map(trinketComponent -> {
                            // 使用 Predicate 检查 BackpackItem
                            List<Tuple<SlotReference, ItemStack>> list = trinketComponent.getEquipped(
                                    stack -> stack.getItem() instanceof BackpackItem
                            );
                            return !list.isEmpty()
                                    ? list.get(0).getA().index()  // 返回槽位索引
                                    : 36+BackpackHelper.getMaxBackpackSize();                         // 未找到时返回默认值
                        })
                        .orElse(36+BackpackHelper.getMaxBackpackSize());
            }

            else if (BackpackPlatform.isModLoaded("accessories") && !BackpackPlatform.isModLoaded("trinkets")) {
                Container container = getContainer(player);
                if (container instanceof AccessoriesContainerAdapter) {
                    return ((AccessoriesContainerAdapter) container).getBackpackSlotIndex();
                }
            }
        }
        return 36+BackpackHelper.getMaxBackpackSize();
    }
}
