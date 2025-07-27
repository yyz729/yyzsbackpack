package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BackpackSorter {
    public static void sortInventorySlots(Player player, NonNullList<Slot> slots) {
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
        sortSlots(inventorySlots);
    }

    public static void sortBackpackSlots(Player player, NonNullList<Slot> slots) {
        List<Slot> backpackSlots = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot instanceof BackpackStorageSlot) {
                backpackSlots.add(slot);
            }
        }
        sortSlots(backpackSlots);
    }

    public static void sortContainerSlots(Player player, Container container, NonNullList<Slot> slots) {
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot slot : slots) {
            // 收集属于目标容器且不是玩家槽位的槽位
            if (slot.container == container && !(slot.container instanceof Inventory)) {
                containerSlots.add(slot);
            }
        }
        sortSlots(containerSlots);
    }

    private static void sortSlots(List<Slot> slotsToSort) {
        // 1. 收集所有非空物品
        List<ItemStack> items = new ArrayList<>();
        for (Slot slot : slotsToSort) {
            if (!slot.getItem().isEmpty()) {
                items.add(slot.getItem().copy());
                slot.set(ItemStack.EMPTY); // 清空槽位
            }
        }

        // 2. 按类型分组并排序
        Map<ResourceLocation, List<ItemStack>> groupedItems = new HashMap<>();
        for (ItemStack stack : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            groupedItems.computeIfAbsent(id, k -> new ArrayList<>()).add(stack);
        }

        // 按物品ID排序
        List<ResourceLocation> sortedIds = new ArrayList<>(groupedItems.keySet());
        Collections.sort(sortedIds);

        // 3. 重新填充槽位（堆叠相同物品）
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

        BackpackStorage.saveBackpackContents(player.getInventory(), back, true);
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
            if (!menu.moveItemStackTo(slotStack, backpackStart, backpackStart + 54, false)) {
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
}
