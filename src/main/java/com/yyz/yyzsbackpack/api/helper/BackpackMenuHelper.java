package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.MenuAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BackpackMenuHelper {
    private BackpackMenuHelper() {}

    public static void addBackpackSlotsIfPresent(AbstractContainerMenu menu, Inventory playerInv) {

        // 计算起始槽位
        int start = menu.slots.size();
        // 存储起始索引
        if (menu instanceof IBackpackMenu backpackMenu) {
            backpackMenu.yyzsbackpack$setBackpackSlotStart(start);
        }

        // 添加 256 个不可见槽位
        for (int i = 0; i < 256; i++) {
            ((MenuAccessor)menu).invokeAddSlot(new BackpackSlot(playerInv, playerInv.getContainerSize() + i, -1000, -1000, playerInv.getContainerSize()));
        }
    }

    public static int getBackpackSlotStart(AbstractContainerMenu menu) {
        if (menu instanceof IBackpackMenu backpackMenu) {
            return backpackMenu.yyzsbackpack$getBackpackSlotStart();
        }
        return -1;
    }

    /**
     * 将物品栏物品移至背包（moveB）
     * @param all true = 全部移动；false = 只移动与背包内已有物品同类型的
     */
    public static void moveIToBackpack(boolean all, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = BackpackMenuHelper.getBackpackSlotStart(menu);
        if (start < 0 || start >= menu.slots.size()) return;

        if (all) {
            moveAll(menu, 0, start, start, menu.slots.size());
        } else {
            // 收集背包内现有物品类型
            List<ItemStack> backpackTypes = collectItems(menu, start, menu.slots.size());
            if (backpackTypes.isEmpty()) return;
            moveMatching(menu, 0, start, start, menu.slots.size(), backpackTypes);
        }
    }

    /**
     * 将背包物品移至物品栏（moveI）
     * @param all true = 全部移动；false = 只移动与物品栏内已有物品同类型的
     */
    public static void moveBToInventory(boolean all, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = BackpackMenuHelper.getBackpackSlotStart(menu);
        if (start < 0 || start >= menu.slots.size()) return;

        if (all) {
            // 全部移动：遍历所有背包槽，直接 quickMoveStack
            for (int i = start; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot instanceof BackpackSlot)) continue;
                if (!slot.getItem().isEmpty()) {
                    menu.quickMoveStack(player, i);
                }
            }
        } else {
            List<ItemStack> inventoryTypes = new ArrayList<>();
            for (int i = 0; i < start; i++) {
                Slot slot = menu.slots.get(i);
                // 只处理 Inventory 容器，且排除快捷栏（槽位 0~8）
                if (!(slot.container instanceof Inventory) || slot.getContainerSlot() < 9) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) {
                    inventoryTypes.add(stack.copy());
                }
            }

            if (inventoryTypes.isEmpty()) return;

            // 遍历背包槽位，匹配并移动
            for (int i = start; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot instanceof BackpackSlot)) continue;
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                boolean matches = inventoryTypes.stream()
                        .anyMatch(type -> ItemStack.isSameItemSameComponents(stack, type));
                if (matches) {
                    menu.quickMoveStack(player, i);
                }
            }
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
                if (!(slot.container instanceof Inventory) || slot.getContainerSlot() < 9) continue;
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                // 尝试将 stack 移动到目标区域
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
                if (!(slot.container instanceof Inventory) || slot.getContainerSlot() < 9) continue;
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                // 检查是否匹配任意类型
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
     * 收集指定槽位范围内的物品（复制一份用于比较）
     */
    private static List<ItemStack> collectItems(AbstractContainerMenu menu, int start, int end) {
        List<ItemStack> types = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Slot slot = menu.slots.get(i);
            // 只处理背包槽位
            if (!(slot instanceof BackpackSlot)) continue;
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                types.add(stack.copy());
            }
        }
        return types;
    }
}