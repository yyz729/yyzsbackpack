package com.yyz.yyzsbackpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreativeTabOrder {
    private static final Map<Item, Integer> ITEM_ORDER = new HashMap<>();
    private static boolean initialized = false;

    public static void buildOrder() {
        if (initialized) return;
        int index = 0;
        // 获取所有创造模式标签页（按原版注册顺序）
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            // 跳过搜索标签页本身，避免循环
            CreativeModeTab searchTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH);
            if (tab == searchTab) continue;
            // 获取该标签页中的物品列表（按显示顺序）
            List<ItemStack> items = getTabContents(tab);
            for (ItemStack stack : items) {
                Item item = stack.getItem();
                if (!ITEM_ORDER.containsKey(item)) {
                    ITEM_ORDER.put(item, index++);
                }
            }
        }
        initialized = true;
    }

    private static List<ItemStack> getTabContents(CreativeModeTab tab) {
        // Forge: 使用 tab.getDisplayItems()
        // Fabric (1.20+): 使用 tab.getDisplayItems() 或自行构建
        // 注意：有时需要传入参数，以下是 Forge 的示例：
        return tab.getDisplayItems().stream().toList();
    }
    public static Comparator<ItemStack> byCreativeSearchOrder() {
        return Comparator.comparingInt(stack -> ITEM_ORDER.getOrDefault(stack.getItem(), Integer.MAX_VALUE));
    }
}