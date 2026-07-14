package com.yyz.yyzsbackpack.api.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SortAlgorithms {
    public static final int CREATIVE = 0;
    public static final int NAME = 1;
    public static final int ID = 2;
    public static final int COUNT = 3;
    public static final int MOD = 4;

    public static final String[] NAMES = {
            "sort.algorithm.creative",
            "sort.algorithm.name",
            "sort.algorithm.id",
            "sort.algorithm.count",
            "sort.algorithm.mod"
    };

    private static final Map<Item, Integer> CREATIVE_ORDER_MAP = new HashMap<>();
    private static boolean creativeOrderInitialized = false;

    public static synchronized void ensureInitialized(MinecraftServer server) {
        if (creativeOrderInitialized) return;
        HolderLookup.Provider registries = server.registryAccess();
        CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
                FeatureFlags.VANILLA_SET, true, registries
        );
        int index = 0;
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (tab.getType() != CreativeModeTab.Type.CATEGORY) continue;
            // 如果 displayItems 为空，主动构建
            if (tab.getDisplayItems().isEmpty()) {
                tab.buildContents(params);
            }
            for (ItemStack stack : tab.getDisplayItems()) {
                Item item = stack.getItem();
                if (CREATIVE_ORDER_MAP.putIfAbsent(item, index) == null) {
                    index++;
                }
            }
        }
        creativeOrderInitialized = true;
    }

    private static int getCreativeOrder(Item item) {
        if (!creativeOrderInitialized) {
            return Integer.MAX_VALUE; // 安全回退
        }
        return CREATIVE_ORDER_MAP.getOrDefault(item, Integer.MAX_VALUE);
    }

    public static Comparator<ItemStack> getComparator(int id) {
        return switch (id) {
            case CREATIVE -> Comparator
                    .comparingInt((ItemStack s) -> getCreativeOrder(s.getItem()))
                    .thenComparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
            case NAME -> Comparator.comparing(s -> s.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
            case ID -> Comparator.comparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
            case COUNT -> Comparator.comparingInt(ItemStack::getCount).reversed();
            case MOD -> Comparator.comparing(s -> {
                Identifier regName = BuiltInRegistries.ITEM.getKey(s.getItem());
                return regName.getNamespace();
            });
            default -> getComparator(NAME);
        };
    }
}