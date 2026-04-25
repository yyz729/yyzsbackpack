package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

public class BackpackSorter {
    public static void sortInventorySlots(Player player, NonNullList<Slot> slots, int type) {
        List<Slot> inventorySlots = new ArrayList<>();
        for (Slot slot : slots) {
            // 只处理玩家物品栏中的主物品栏槽位 (9-35)
            if (slot.container instanceof Inventory) {
                int index = slot.getContainerSlot();
                if (index >= 9 && index < 36) {
                    inventorySlots.add(slot);
                }
            }
        }

        sortSlots(inventorySlots, type);
    }

    public static void sortBackpackSlots(Player player, NonNullList<Slot> slots, int type) {
        List<Slot> backpackSlots = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot instanceof BackpackStorageSlot) {
                backpackSlots.add(slot);
            }
        }

        sortSlots(backpackSlots, type);
    }

    public static void sortContainerSlots(Player player, Container container, NonNullList<Slot> slots, int type) {
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot slot : slots) {
            // 收集属于目标容器且不是玩家槽位的槽位
            if (slot.container == container && !(slot.container instanceof Inventory)) {
                containerSlots.add(slot);
            }
        }

        sortSlots(containerSlots,type);
    }


    private static final Map<Item, Integer> creativeOrderMap = new HashMap<>();
    private static boolean initialized = false;

    private static final Map<Item, Integer> customOrderMap = new HashMap<>();
    private static boolean customOrderLoaded = false;
    private static final String CUSTOM_ORDER_FILE = "config/yyzsbackpack/sorter_custom.json";

    /**
     * 创建默认的自定义排序配置文件
     */
    public static void loadDefaultCustomSort() {
        java.nio.file.Path configPath = java.nio.file.Paths.get(CUSTOM_ORDER_FILE);
        try {
            // 确保父目录存在
            java.nio.file.Files.createDirectories(configPath.getParent());
            // 默认 JSON 内容
            String defaultContent = "[\n" +
                    "  \"minecraft:diamond\",\n" +
                    "  \"minecraft:iron_ingot\",\n" +
                    "  \"minecraft:gold_ingot\",\n" +
                    "  \"minecraft:emerald\"\n" +
                    "]";
            // 写入文件
            java.nio.file.Files.writeString(configPath, defaultContent);
            Backpack.LOGGER.info("已生成默认自定义排序配置文件: {}", CUSTOM_ORDER_FILE);
        } catch (Exception e) {
            Backpack.LOGGER.error("生成默认自定义排序文件失败: {}", e.getMessage());
        }
    }

    private static void loadCustomOrder() {
        if (customOrderLoaded) return;
        customOrderMap.clear();
        java.nio.file.Path configPath = java.nio.file.Paths.get(CUSTOM_ORDER_FILE);
        if (!java.nio.file.Files.exists(configPath)) {
            Backpack.LOGGER.warn("自定义排序文件不存在，将生成默认配置: {}", CUSTOM_ORDER_FILE);
            loadDefaultCustomSort();  // 生成默认文件
        }
        try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(configPath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString().trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                String content = json.substring(1, json.length() - 1);
                List<String> items = new ArrayList<>();
                boolean inQuotes = false;
                StringBuilder current = new StringBuilder();
                for (char c : content.toCharArray()) {
                    if (c == '"') {
                        inQuotes = !inQuotes;
                    } else if (c == ',' && !inQuotes) {
                        items.add(current.toString().trim());
                        current.setLength(0);
                    } else {
                        current.append(c);
                    }
                }
                if (current.length() > 0) {
                    items.add(current.toString().trim());
                }
                int index = 0;
                for (String itemStr : items) {
                    // 去除两端引号
                    if (itemStr.startsWith("\"") && itemStr.endsWith("\"")) {
                        itemStr = itemStr.substring(1, itemStr.length() - 1);
                    }
                    if (!itemStr.isEmpty()) {
                        ResourceLocation id = ResourceLocation.tryParse(itemStr);
                        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                            Item item = BuiltInRegistries.ITEM.get(id);
                            customOrderMap.putIfAbsent(item, index++);
                        } else {
                            Backpack.LOGGER.warn("无效的物品ID: {}", itemStr);
                        }
                    }
                }
                Backpack.LOGGER.info("已加载自定义排序配置，共 {} 个物品", customOrderMap.size());
            } else {
                Backpack.LOGGER.error("自定义排序文件格式错误，应为JSON数组: {}", CUSTOM_ORDER_FILE);
            }
        } catch (Exception e) {
            Backpack.LOGGER.error("读取自定义排序文件失败: {}", e.getMessage());
        }
        customOrderLoaded = true;
    }

    public static void reloadCustomOrder() {
        customOrderLoaded = false;
        customOrderMap.clear();
        loadCustomOrder();
    }

    private static void initCreativeOrder() {
        if (initialized) return;
        int index = 0;
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            for (ItemStack stack : tab.getDisplayItems()) {
                Item item = stack.getItem();
                creativeOrderMap.putIfAbsent(item, index++);
            }
        }
        initialized = true;
    }


    private static void sortSlots(List<Slot> slotsToSort, int type) {
        //收集所有非空物品
        List<ItemStack> items = new ArrayList<>();
        for (Slot slot : slotsToSort) {
            if (!slot.getItem().isEmpty()) {
                items.add(slot.getItem().copy());
                slot.set(ItemStack.EMPTY); // 清空槽位
            }
        }

        //按类型分组并排序
        Map<ResourceLocation, List<ItemStack>> groupedItems = new HashMap<>();
        for (ItemStack stack : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            groupedItems.computeIfAbsent(id, k -> new ArrayList<>()).add(stack);
        }


        List<ResourceLocation> sortedIds = new ArrayList<>(groupedItems.keySet());
        initCreativeOrder();
        switch (type){
            case 2:
                System.out.println("执行创造模式排序");
                //按创造模式物品栏排序
                sortedIds.sort((id1, id2) -> {
                    Item item1 = BuiltInRegistries.ITEM.get(id1);
                    Item item2 = BuiltInRegistries.ITEM.get(id2);
                    int order1 = creativeOrderMap.getOrDefault(item1, Integer.MAX_VALUE);
                    int order2 = creativeOrderMap.getOrDefault(item2, Integer.MAX_VALUE);
                    return Integer.compare(order1, order2);
                });
                break;
            case 3:
                System.out.println("执行 ID 排序");
                Collections.sort(sortedIds);
                break;
            case 4:
                System.out.println("执行稀有度排序");
                sortedIds.sort((id1, id2) -> {
                    List<ItemStack> stacks1 = groupedItems.get(id1);
                    List<ItemStack> stacks2 = groupedItems.get(id2);
                    if (stacks1 == null || stacks1.isEmpty()) return 1;
                    if (stacks2 == null || stacks2.isEmpty()) return -1;

                    ItemStack sample1 = stacks1.get(0);
                    ItemStack sample2 = stacks2.get(0);

                    Rarity rarity1 = sample1.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
                    Rarity rarity2 = sample2.getOrDefault(DataComponents.RARITY, Rarity.COMMON);

                    // 稀有度降序（高稀有度在前）
                    int cmp = Integer.compare(rarity2.ordinal(), rarity1.ordinal());
                    if (cmp != 0) return cmp;

                    // 稀有度相同时，按创造模式物品栏顺序排序
                    Item item1 = BuiltInRegistries.ITEM.get(id1);
                    Item item2 = BuiltInRegistries.ITEM.get(id2);
                    int order1 = creativeOrderMap.getOrDefault(item1, Integer.MAX_VALUE);
                    int order2 = creativeOrderMap.getOrDefault(item2, Integer.MAX_VALUE);
                    return Integer.compare(order1, order2);
                });
                break;
            case 5:
                System.out.println("执行自定义排序");
                loadCustomOrder(); // 确保加载
                sortedIds.sort((id1, id2) -> {
                    Item item1 = BuiltInRegistries.ITEM.get(id1);
                    Item item2 = BuiltInRegistries.ITEM.get(id2);
                    // 优先使用自定义顺序，否则回退到创造模式顺序，最后 MAX_VALUE
                    int order1 = customOrderMap.getOrDefault(item1,
                            creativeOrderMap.getOrDefault(item1, Integer.MAX_VALUE));
                    int order2 = customOrderMap.getOrDefault(item2,
                            creativeOrderMap.getOrDefault(item2, Integer.MAX_VALUE));
                    return Integer.compare(order1, order2);
                });
                break;
            default:
        }

        System.out.println(type);

        //重新填充槽位（堆叠相同物品）
        int slotIndex = 0;
        for (ResourceLocation id : sortedIds) {
            List<ItemStack> stacks = groupedItems.get(id);

            // 合并相同物品
            List<ItemStack> merged = mergeStacks(stacks);

            for (ItemStack stack : merged) {
                if (slotIndex >= slotsToSort.size()) break;
                slotsToSort.get(slotIndex++).set(stack);
            }
        }
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        if (stacks.isEmpty()) return merged;

        ItemStack current = stacks.get(0).copy();

        for (int i = 1; i < stacks.size(); i++) {
            ItemStack next = stacks.get(i);

            // 如果相同且可堆叠
            if (ItemStack.isSameItemSameComponents(current, next)) {
                int space = current.getMaxStackSize() - current.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, next.getCount());
                    current.grow(transfer);
                    next.shrink(transfer);
                }

                if (next.getCount() <= 0) continue;
            }

            // 当前堆栈已满，添加到结果
            merged.add(current);
            current = next.copy();
        }

        merged.add(current);
        return merged;
    }

    public static void handleBackpackSwap(AbstractContainerMenu menu,
                                          NonNullList<Slot> slots,
                                          ItemStack carried,
                                          int slotIndex,
                                          int button,
                                          ClickType actionType,
                                          Player player) {
        if (slotIndex < 0 || slotIndex >= slots.size() ||
                actionType != ClickType.PICKUP ||
                slots.get(slotIndex).getItem().getItem() instanceof BackpackItem ||
                !Backpack.getConfig().quick_swap_backpack) return;

        if (!(menu.getSlot(slotIndex) instanceof BackpackStorageSlot)) return;

        ItemStack back = BackpackPlatform.getEquipped(player).copy();
        ItemStack stack = carried.copy();

        if (!(back.getItem() instanceof BackpackItem) ||
                !(stack.getItem() instanceof BackpackItem)) return;

        BackpackStorage.saveBackpackContents(player.getInventory(), back);
        BackpackStorage.restoreBackpackContents(player.getInventory(), stack);
        Container container = BackpackPlatform.getContainer(player);
        container.setItem(BackpackPlatform.getIndex(player), stack);
        menu.setCarried(back);
    }


    public static ItemStack quickMoveToHotbar(AbstractContainerMenu menu, Player player, int slotIndex, NonNullList<Slot> slots) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);


        // 动态获取快捷栏索引范围
        int hotbarStart = -1;
        int hotbarEnd = -1;
        for (int idx = 0; idx < slots.size(); idx++) {
            Slot s = slots.get(idx);
            if (s.container instanceof Inventory &&
                    s.getContainerSlot() >= 0 &&
                    s.getContainerSlot() < 9) {
                if (hotbarStart == -1) hotbarStart = idx;
                hotbarEnd = idx + 1; // 结束索引是exclusive的
            }
        }

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // 尝试移动到快捷栏 (36-44)
            if (!menu.moveItemStackTo(slotStack, hotbarStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }


    public static ItemStack quickMoveToBackpack(AbstractContainerMenu menu, Player player, int slotIndex, NonNullList<Slot> slots) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // 查找背包槽位起始索引
            int backpackStart = -1;
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i) instanceof BackpackStorageSlot) {
                    backpackStart = i;
                    break;
                }
            }

            if (backpackStart == -1) {
                return ItemStack.EMPTY;
            }

            // 尝试移动到背包槽位
            if (!menu.moveItemStackTo(slotStack, backpackStart, backpackStart + BackpackHelper.getMaxBackpackSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    public static void quickMoveTo(AbstractContainerMenu menu, NonNullList<Slot> slots, int i, int j, ClickType clickType, Player player, CallbackInfo ci){
        if (clickType == ClickType.QUICK_MOVE && j == 1) {
            if (i < 0) {
                ci.cancel();
                return;
            }

            Slot slot = (Slot)slots.get(i);
            if (!slot.mayPickup(player)) {
                ci.cancel();
                return;
            }

            // 如果是背包槽位，转移到快捷栏
            if (slot instanceof BackpackStorageSlot) {
                for (ItemStack itemStack = BackpackSorter.quickMoveToHotbar(menu,player, i,slots);
                     !itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack);
                     itemStack = BackpackSorter.quickMoveToHotbar(menu,player, i,slots)) {
                }
                ci.cancel();
            }

            // 如果是原版槽位，转移到背包
            else  { // 物品栏和快捷栏槽位
                for (ItemStack itemStack = BackpackSorter.quickMoveToBackpack(menu,player, i,slots);
                     !itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack);
                     itemStack = BackpackSorter.quickMoveToBackpack(menu,player, i,slots)) {
                }
                ci.cancel();
            }
        }
        else if (clickType == ClickType.QUICK_MOVE && (j == 2 || j == 3 || j == 4 || j == 5)) {
            Slot hoveredSlot = (Slot) slots.get(i);

            // 动态识别玩家物品栏槽位
            boolean isInventorySlot = false;
            if (hoveredSlot.container instanceof Inventory) {
                int slotIndexInPlayerInv = hoveredSlot.getContainerSlot();
                // 主物品栏范围：9-35 (不包括快捷栏0-8和护甲36-39)
                if (slotIndexInPlayerInv >= 9 && slotIndexInPlayerInv < 36) {
                    isInventorySlot = true;
                }
            }

            boolean isBackpackSlot = hoveredSlot instanceof BackpackStorageSlot;
            boolean isContainerSlot = !isInventorySlot && !isBackpackSlot &&
                    hoveredSlot.container != player.getInventory();

            if (!isInventorySlot && !isBackpackSlot && !isContainerSlot) return;

            if (isInventorySlot) {
                BackpackSorter.sortInventorySlots(player,slots,j);
            } else if (isBackpackSlot) {
                BackpackSorter.sortBackpackSlots(player,slots,j);
            } else {
                BackpackSorter.sortContainerSlots(player, hoveredSlot.container,slots,j);
            }
        }
    }
}
