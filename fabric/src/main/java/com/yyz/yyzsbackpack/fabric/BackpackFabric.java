package com.yyz.yyzsbackpack.fabric;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import com.yyz.yyzsbackpack.compat.AccessoriesContainerAdapter;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import java.util.List;
import java.util.Map;

import static com.yyz.yyzsbackpack.Backpack.MOD_ID;

public final class BackpackFabric implements ModInitializer {
    public static final BackpackItem WOOLEN_BACKPACK = new BackpackItem(BackpackMaterial.WOOLEN, new Item.Properties().stacksTo(1));
    public static final BackpackItem STONE_BACKPACK = new BackpackItem(BackpackMaterial.STONE, new Item.Properties().stacksTo(1));
    public static final BackpackItem IRON_BACKPACK = new BackpackItem(BackpackMaterial.IRON, new Item.Properties().stacksTo(1));
    public static final BackpackItem GOLD_BACKPACK = new BackpackItem(BackpackMaterial.GOLD, new Item.Properties().stacksTo(1));
    public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem(BackpackMaterial.DIAMOND, new Item.Properties().stacksTo(1));
    public static final BackpackItem NETHERITE_BACKPACK = new BackpackItem(BackpackMaterial.NETHERITE, new Item.Properties().stacksTo(1).fireResistant());

    public static final CreativeModeTab GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GOLD_BACKPACK))
            .title(Component.translatable("itemGroup.yyzsbackpack.title"))
            .displayItems((context, entries) -> {
                // 按顺序添加所有背包到物品组
                entries.accept(WOOLEN_BACKPACK);
                entries.accept(STONE_BACKPACK);
                entries.accept(IRON_BACKPACK);
                entries.accept(GOLD_BACKPACK);
                entries.accept(DIAMOND_BACKPACK);
                entries.accept(NETHERITE_BACKPACK);
            })
            .build();

    public static void register(){
        // 注册所有背包物品
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "woolen_backpack"), WOOLEN_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "stone_backpack"), STONE_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "iron_backpack"), IRON_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "gold_backpack"), GOLD_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "diamond_backpack"), DIAMOND_BACKPACK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "netherite_backpack"), NETHERITE_BACKPACK);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "group"), GROUP);
    }

    @Override
    public void onInitialize() {
        register();
        Backpack.init();

    }

    public static ItemStack getEquipped(Player player) {
        if(!Backpack.getConfig().force_slot) {
            if (BackpackHelper.isModLoaded("trinkets")) {
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
            // 2. 检查 Accessories
            else if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("trinkets")) {
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
        return player.getInventory().getItem(36);
    }

    public static Container getContainer(Player player) {
        if(!Backpack.getConfig().force_slot) {
            if (BackpackHelper.isModLoaded("trinkets")) {
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
            else if (BackpackHelper.isModLoaded("accessories")  && !BackpackHelper.isModLoaded("trinkets")) {
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
            if (BackpackHelper.isModLoaded("trinkets") ) {
                return TrinketsApi.getTrinketComponent(player)
                        .map(trinketComponent -> {
                            // 使用 Predicate 检查 BackpackItem
                            List<Tuple<SlotReference, ItemStack>> list = trinketComponent.getEquipped(
                                    stack -> stack.getItem() instanceof BackpackItem
                            );
                            return !list.isEmpty()
                                    ? list.get(0).getA().index()  // 返回槽位索引
                                    : 36;                         // 未找到时返回默认值
                        })
                        .orElse(36);
            }

            else if (BackpackHelper.isModLoaded("accessories")  && !BackpackHelper.isModLoaded("trinkets")) {
                Container container = getContainer(player);
                if (container instanceof AccessoriesContainerAdapter) {
                    return ((AccessoriesContainerAdapter) container).getBackpackSlotIndex();
                }
            }
        }
        return 36;
    }
}
