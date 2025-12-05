package com.yyz.yyzsbackpack.fabric;

import com.mojang.serialization.Codec;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
//import dev.emi.trinkets.api.SlotReference;
//import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Function;

import static com.yyz.yyzsbackpack.Backpack.MOD_ID;

public final class BackpackFabric implements ModInitializer {

    public static final BackpackItem IRON_BACKPACK = (BackpackItem) register("iron_backpack", props -> new BackpackItem("iron", props), new Item.Properties().stacksTo(1));
    public static final BackpackItem GOLD_BACKPACK = (BackpackItem) register("gold_backpack", props -> new BackpackItem("gold", props), new Item.Properties().stacksTo(1));
    public static final BackpackItem DIAMOND_BACKPACK = (BackpackItem) register("diamond_backpack", props -> new BackpackItem("diamond", props), new Item.Properties().stacksTo(1));
    public static final BackpackItem NETHERITE_BACKPACK = (BackpackItem) register("netherite_backpack", props -> new BackpackItem("netherite", props), new Item.Properties().stacksTo(1).fireResistant());
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

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "backpack_items"), BACKPACK_ITEMS_COMPONENT);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "group"), GROUP);
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            Backpack.registerCommands(dispatcher);
        });
    }

    public static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {

        final ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        return Items.registerItem(registryKey, factory, settings);
    }

    @Override
    public void onInitialize() {

        register();
        Backpack.init();

    }
    public static ItemStack getEquipped(Player player) {
        if (BackpackPlatform.isModLoaded("trinkets") && !Backpack.getConfig().use_dedicated_slot) {
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
        return player.getInventory().getItem(36+ BackpackHelper.getMaxBackpackSize());
    }
    public static ItemStack getEquippedL(LivingEntity player) {
        if (BackpackPlatform.isModLoaded("trinkets") && !Backpack.getConfig().use_dedicated_slot) {
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
        return player.getSlot(36+ BackpackHelper.getMaxBackpackSize()).get();
    }

    public static Container getContainer(Player player) {
        if (BackpackPlatform.isModLoaded("trinkets")&& !Backpack.getConfig().use_dedicated_slot) {
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
        return player.getInventory();
    }

    public static int getIndex(Player player) {
        if (BackpackPlatform.isModLoaded("trinkets")&& !Backpack.getConfig().use_dedicated_slot) {
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
        return 36+BackpackHelper.getMaxBackpackSize();
    }
}
