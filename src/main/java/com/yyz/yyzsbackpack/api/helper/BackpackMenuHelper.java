package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.MenuAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BackpackMenuHelper {
    private BackpackMenuHelper() {}



    public static void addBackpackSlotsIfPresent(AbstractContainerMenu menu, Inventory playerInv) {
        // 添加 256 个槽位，但操作时会按实际容量限制
        for (int i = 0; i < 256; i++) {
            ((MenuAccessor)menu).invokeAddSlot(new BackpackSlot(playerInv, playerInv.getContainerSize() + i, -1000, -1000, playerInv.getContainerSize()));
        }
    }

    public static int getBackpackSlotStart(AbstractContainerMenu menu) {
        for (int i = 0; i < menu.slots.size(); i++) {
            if (menu.slots.get(i) instanceof BackpackSlot) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将物品栏物品移至背包（moveB）
     * @param all true = 全部移动；false = 只移动与背包内已有物品同类型的
     */
    public static void moveIToBackpack(boolean all, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = getBackpackSlotStart(menu);
        if (start < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int end = Math.min(start + size, menu.slots.size());

        // 获取主物品栏范围（9~35）
        int[] invMain = findInventoryMainRange(menu, player);
        if (invMain == null) return;
        int invStart = invMain[0], invEnd = invMain[1];

        if (all) {
            moveAll(menu, invStart, invEnd, start, end);
        } else {
            // 收集背包类型（不变）
            List<ItemStack> backpackTypes = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot instanceof BackpackSlot)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) backpackTypes.add(stack.copy());
            }
            if (backpackTypes.isEmpty()) return;
            moveMatching(menu, invStart, invEnd, start, end, backpackTypes);
        }
    }

    /**
     * 将背包物品移至物品栏（moveI）
     * @param all true = 全部移动；false = 只移动与物品栏内已有物品同类型的
     */
    public static void moveBToInventory(boolean all, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = getBackpackSlotStart(menu);
        if (start < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int end = Math.min(start + size, menu.slots.size());

        // 目标：主物品栏
        int[] invMain = findInventoryMainRange(menu, player);
        if (invMain == null) return;
        int targetStart = invMain[0], targetEnd = invMain[1];

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        if (all) {
            boolean moved;
            do {
                moved = false;
                for (int i = start; i < end; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot instanceof BackpackSlot)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, targetStart, targetEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                        else slot.set(stack);
                    }
                }
            } while (moved);
        } else {
            // 同类：收集主物品栏已有物品类型
            List<ItemStack> inventoryTypes = new ArrayList<>();
            for (int i = targetStart; i < targetEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container != player.getInventory()) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) inventoryTypes.add(stack.copy());
            }
            if (inventoryTypes.isEmpty()) return;
            boolean moved;
            do {
                moved = false;
                for (int i = start; i < end; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot instanceof BackpackSlot)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    boolean matches = inventoryTypes.stream()
                            .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                    if (matches) {
                        if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, targetStart, targetEnd, false)) {
                            moved = true;
                            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                            else slot.set(stack);
                        }
                    }
                }
            } while (moved);
        }
    }

    /**
     * 移动所有物品（循环直到无法再移动）
     */
    private static void moveAll(AbstractContainerMenu menu,
                                int sourceStart, int sourceEnd,
                                int targetStart, int targetEnd) {
        if (!(menu instanceof IBackpackMenu backpackMenu)) return;
        boolean moved;
        do {
            moved = false;
            for (int i = sourceStart; i < sourceEnd; i++) {
                Slot slot = menu.slots.get(i);
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, targetStart, targetEnd, false)) {
                    moved = true;
                    if (stack.isEmpty()) {
                        slot.set(ItemStack.EMPTY);
                    } else {
                        slot.set(stack);
                    }
                }
            }
        } while (moved);
    }

    /**
     * 仅移动与 matchTypes 中任一物品类型匹配的物品
     */
    private static void moveMatching(AbstractContainerMenu menu,
                                     int sourceStart, int sourceEnd,
                                     int targetStart, int targetEnd,
                                     List<ItemStack> matchTypes) {
        if (!(menu instanceof IBackpackMenu backpackMenu)) return;
        boolean moved;
        do {
            moved = false;
            for (int i = sourceStart; i < sourceEnd; i++) {
                Slot slot = menu.slots.get(i);
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                boolean matches = matchTypes.stream()
                        .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                if (matches) {
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, targetStart, targetEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) {
                            slot.set(ItemStack.EMPTY);
                        } else {
                            slot.set(stack);
                        }
                    }
                }
            }
        } while (moved);
    }

    /**
     * 将容器（BaseContainerBlockEntity）中的物品移至玩家主物品栏（9~35）
     * @param all true=全部移动，false=只移动与主物品栏内已有物品同类型的
     */
    public static void moveCToInventory(boolean all, ServerPlayer player) {
        Backpack.LOGGER.info("Moving c to Inventory");
        AbstractContainerMenu menu = player.containerMenu;
        int[] containerRange = findContainerRange(menu,player);
        if (containerRange == null) return;
        int containerStart = containerRange[0], containerEnd = containerRange[1];

        // 只使用主物品栏（9~35）作为目标
        int[] invMainRange = findInventoryMainRange(menu, player);
        if (invMainRange == null) return;
        int invStart = invMainRange[0], invEnd = invMainRange[1];

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        if (all) {
            boolean moved;
            do {
                moved = false;
                for (int i = containerStart; i < containerEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, invStart, invEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                        else slot.set(stack);
                    }
                }
            } while (moved);
        } else {
            List<ItemStack> inventoryTypes = new ArrayList<>();
            for (int i = invStart; i < invEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot.container instanceof Inventory)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) inventoryTypes.add(stack.copy());
            }
            if (inventoryTypes.isEmpty()) return;

            boolean moved;
            do {
                moved = false;
                for (int i = containerStart; i < containerEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    boolean matches = inventoryTypes.stream()
                            .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                    if (matches) {
                        if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, invStart, invEnd, false)) {
                            moved = true;
                            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                            else slot.set(stack);
                        }
                    }
                }
            } while (moved);
        }
    }

    /**
     * 将玩家主物品栏（9~35）中的物品移至容器
     */
    public static void moveIToContainer(boolean all, ServerPlayer player) {
        Backpack.LOGGER.info("Moving I to Container");
        AbstractContainerMenu menu = player.containerMenu;
        int[] invMainRange = findInventoryMainRange(menu, player);
        if (invMainRange == null) return;
        int invStart = invMainRange[0], invEnd = invMainRange[1];

        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;
        int containerStart = containerRange[0], containerEnd = containerRange[1];

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        if (all) {
            boolean moved;
            do {
                moved = false;
                for (int i = invStart; i < invEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof Inventory)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, containerStart, containerEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                        else slot.set(stack);
                    }
                }
            } while (moved);
        } else {
            List<ItemStack> containerTypes = new ArrayList<>();
            for (int i = containerStart; i < containerEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) containerTypes.add(stack.copy());
            }
            if (containerTypes.isEmpty()) return;

            boolean moved;
            do {
                moved = false;
                for (int i = invStart; i < invEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof Inventory)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    boolean matches = containerTypes.stream()
                            .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                    if (matches) {
                        if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, containerStart, containerEnd, false)) {
                            moved = true;
                            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                            else slot.set(stack);
                        }
                    }
                }
            } while (moved);
        }
    }

    /**
     * 将容器中的物品移至背包
     */
    public static void moveCToBackpack(boolean all, ServerPlayer player) {
        Backpack.LOGGER.info("Moving C to Backpack");
        AbstractContainerMenu menu = player.containerMenu;
        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;
        int containerStart = containerRange[0], containerEnd = containerRange[1];

        int backpackStart = getBackpackSlotStart(menu);
        if (backpackStart < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int backpackEnd = Math.min(backpackStart + size, menu.slots.size());

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        if (all) {
            boolean moved;
            do {
                moved = false;
                for (int i = containerStart; i < containerEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, backpackStart, backpackEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                        else slot.set(stack);
                    }
                }
            } while (moved);
        } else {
            List<ItemStack> backpackTypes = new ArrayList<>();
            for (int i = backpackStart; i < backpackEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot instanceof BackpackSlot)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) backpackTypes.add(stack.copy());
            }
            if (backpackTypes.isEmpty()) return;

            boolean moved;
            do {
                moved = false;
                for (int i = containerStart; i < containerEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    boolean matches = backpackTypes.stream()
                            .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                    if (matches) {
                        if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, backpackStart, backpackEnd, false)) {
                            moved = true;
                            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                            else slot.set(stack);
                        }
                    }
                }
            } while (moved);
        }
    }

    /**
     * 将背包中的物品移至容器
     */
    public static void moveBToContainer(boolean all, ServerPlayer player) {
        Backpack.LOGGER.info("Moving B to Container");
        AbstractContainerMenu menu = player.containerMenu;
        int backpackStart = getBackpackSlotStart(menu);
        if (backpackStart < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int backpackEnd = Math.min(backpackStart + size, menu.slots.size());

        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;
        int containerStart = containerRange[0], containerEnd = containerRange[1];

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        if (all) {
            boolean moved;
            do {
                moved = false;
                for (int i = backpackStart; i < backpackEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot instanceof BackpackSlot)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, containerStart, containerEnd, false)) {
                        moved = true;
                        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                        else slot.set(stack);
                    }
                }
            } while (moved);
        } else {
            List<ItemStack> containerTypes = new ArrayList<>();
            for (int i = containerStart; i < containerEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot.container instanceof BaseContainerBlockEntity)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) containerTypes.add(stack.copy());
            }
            if (containerTypes.isEmpty()) return;

            boolean moved;
            do {
                moved = false;
                for (int i = backpackStart; i < backpackEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (!(slot instanceof BackpackSlot)) continue;
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    boolean matches = containerTypes.stream()
                            .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                    if (matches) {
                        if (backpackMenu.yyzsbackpack$moveItemStackTo(stack, containerStart, containerEnd, false)) {
                            moved = true;
                            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
                            else slot.set(stack);
                        }
                    }
                }
            } while (moved);
        }
    }


    /**
     * 查找菜单中所有属于 BaseContainerBlockEntity 的槽位范围（连续）
     */
    public static int[] findContainerRange(AbstractContainerMenu menu, ServerPlayer player) {
        int start = -1, end = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container instanceof BaseContainerBlockEntity || slot.container instanceof CompoundContainer) {
                if (start == -1) start = i;
                end = i;
            }
        }
        return (start == -1) ? null : new int[]{start, end + 1};
    }

    /**
     * 查找玩家主物品栏（索引 9~35）对应的菜单槽位范围
     */
    public static int[] findInventoryMainRange(AbstractContainerMenu menu, Player player) {
        Inventory inv = player.getInventory();
        int start = -1, end = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container == inv) {
                int idx = slot.getContainerSlot();
                if (idx >= 9 && idx < 36) {   // 只主物品栏
                    if (start == -1) start = i;
                    end = i;
                }
            }
        }
        return (start == -1) ? null : new int[]{start, end + 1};
    }

    public static void sortSlots(AbstractContainerMenu menu, int start, int end, Comparator<ItemStack> comparator) {
        // 收集物品
        List<ItemStack> items = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof BackpackItem) continue;
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
            slot.set(ItemStack.EMPTY);
        }
        // 合并 + 排序
        List<ItemStack> sorted = mergeAndSortItems(items, comparator);
        // 填充
        fillSlots(menu, start, end, sorted);
    }

    public static List<ItemStack> mergeAndSortItems(List<ItemStack> items, Comparator<ItemStack> comparator) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : items) {
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    existing.grow(stack.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(stack.copy());
            }
        }
        merged.sort(comparator);
        return merged;
    }

    private static void fillSlots(AbstractContainerMenu menu, int start, int end, List<ItemStack> items) {
        int itemIdx = 0;
        for (int slotIdx = start; slotIdx < end && itemIdx < items.size(); slotIdx++) {
            Slot slot = menu.slots.get(slotIdx);
            // 如果槽位非空，跳过
            if (slot.hasItem()) continue;

            ItemStack toPlace = items.get(itemIdx);
            int remaining = toPlace.getCount();
            int maxStack = toPlace.getMaxStackSize();

            while (remaining > 0 && slotIdx < end) {
                Slot currentSlot = menu.slots.get(slotIdx);
                // 再次检查是否为空，非空则推进
                if (currentSlot.hasItem()) {
                    slotIdx++;
                    continue;
                }
                int put = Math.min(remaining, maxStack);
                ItemStack putStack = toPlace.copyWithCount(put);
                currentSlot.set(putStack);
                remaining -= put;
                if (remaining > 0) {
                    slotIdx++;
                    if (slotIdx >= end) break;
                } else {
                    itemIdx++;
                    break;
                }
            }
            if (remaining > 0) break;
        }
    }

    /**
     * 将物品堆尝试移动到指定的三个区域中，支持自定义各区域范围和优先级顺序。
     * 注意：三个区间应当互不重叠，且按实际槽位顺序排列（主物品栏 → 快捷栏 → 背包）。
     *
     * @param itemStack   要移动的物品堆（会被修改）
     * @param mainStart   主物品栏起始索引
     * @param mainEnd     主物品栏结束索引（不包含）
     * @param hotbarStart  快捷栏起始索引
     * @param hotbarEnd    快捷栏结束索引（不包含）
     * @param backpackStart    背包起始索引
     * @param backpackEnd      背包结束索引（不包含）
     * @param backwards   true 表示启用优先策略，false 表示按普通正序处理所有指定区间
     * @return 是否发生了任何移动（合并或放入空槽）
     */
    public static boolean moveItemStackToWithBackpack(AbstractContainerMenu menu, ItemStack itemStack,
                                                      int mainStart, int mainEnd,
                                                      int hotbarStart, int hotbarEnd,
                                                      int backpackStart, int backpackEnd,
                                                      boolean backwards) {
        if (itemStack.isEmpty()) {
            return false;
        }

        boolean anyMoved = false;

        // 构建槽位遍历列表
        IntList slotsToProcess = new IntArrayList();
        if (backwards) {
            // 快捷栏倒序 → 主物品栏倒序 → 背包正序
            for (int i = hotbarEnd - 1; i >= hotbarStart; i--) slotsToProcess.add(i);
            for (int i = mainEnd - 1; i >= mainStart; i--) slotsToProcess.add(i);
            for (int i = backpackStart; i < backpackEnd; i++) slotsToProcess.add(i);
        } else {
            // 按主物品栏 → 快捷栏 → 背包 的正序
            for (int i = mainStart; i < mainEnd; i++) slotsToProcess.add(i);
            for (int i = hotbarStart; i < hotbarEnd; i++) slotsToProcess.add(i);
            for (int i = backpackStart; i < backpackEnd; i++) slotsToProcess.add(i);
        }

        // 尝试合并到已有相同物品的槽位
        for (int idx : slotsToProcess) {
            if (tryMergeIntoSlot(menu, itemStack, idx)) {
                anyMoved = true;
                if (itemStack.isEmpty()) break;
            }
        }

        // 尝试放入空槽位
        if (!itemStack.isEmpty()) {
            for (int idx : slotsToProcess) {
                if (tryPlaceIntoEmptySlot(menu, itemStack, idx)) {
                    anyMoved = true;
                    if (itemStack.isEmpty()) break;
                }
            }
        }

        return anyMoved;
    }
    
    private static boolean tryMergeIntoSlot(AbstractContainerMenu menu, ItemStack itemStack, int slotIndex) {
        Slot slot = menu.slots.get(slotIndex);
        ItemStack slotStack = slot.getItem();
        if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slotStack)) {
            int maxStack = Math.min(slot.getMaxStackSize(slotStack), itemStack.getMaxStackSize());
            int total = slotStack.getCount() + itemStack.getCount();
            if (total <= maxStack) {
                itemStack.setCount(0);
                slotStack.setCount(total);
                slot.setChanged();
                return true;
            } else if (slotStack.getCount() < maxStack) {
                int toAdd = maxStack - slotStack.getCount();
                itemStack.shrink(toAdd);
                slotStack.setCount(maxStack);
                slot.setChanged();
                return true;
            }
        }
        return false;
    }
    
    private static boolean tryPlaceIntoEmptySlot(AbstractContainerMenu menu, ItemStack itemStack, int slotIndex) {
        Slot slot = menu.slots.get(slotIndex);
        if (!slot.hasItem() && slot.mayPlace(itemStack)) {
            int maxStack = Math.min(slot.getMaxStackSize(itemStack), itemStack.getMaxStackSize());
            slot.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), maxStack)));
            slot.setChanged();
            return true;
        }
        return false;
    }
}