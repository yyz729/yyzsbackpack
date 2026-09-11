package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.api.IVirtualContainer;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
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

import java.util.*;

public final class BackpackMenuHelper {
    private BackpackMenuHelper() {}

    // 全局生效的容器类
    private static final Set<Class<?>> GLOBAL_EXTERNAL_CONTAINER_CLASSES = new HashSet<>();

    // 特定菜单中生效的容器类
    private static final Map<Class<? extends AbstractContainerMenu>, Set<Class<?>>> MENU_SPECIFIC_CONTAINERS = new HashMap<>();

    private static final List<IVirtualContainer> VIRTUAL_HANDLERS = new ArrayList<>();

    static {
        GLOBAL_EXTERNAL_CONTAINER_CLASSES.add(BaseContainerBlockEntity.class);
        GLOBAL_EXTERNAL_CONTAINER_CLASSES.add(CompoundContainer.class);
    }

    /**
     * 注册一个全局外部容器类，所有菜单中都识别为外部容器。
     */
    public static void registerExternalContainer(Class<?> containerClass) {
        if (containerClass != null) {
            GLOBAL_EXTERNAL_CONTAINER_CLASSES.add(containerClass);
        }
    }

    /**
     * 注册一个仅在指定菜单类中生效的外部容器类。
     */
    public static void registerExternalContainer(Class<?> containerClass, Class<? extends AbstractContainerMenu> menuClass) {
        if (containerClass == null || menuClass == null) return;
        MENU_SPECIFIC_CONTAINERS
                .computeIfAbsent(menuClass, k -> new HashSet<>())
                .add(containerClass);
    }

    /**
     * 注册虚拟容器处理器（用于终端类、网络存储等没有真实 Slot 的界面）
     */
    public static void registerVirtualContainerHandler(IVirtualContainer handler) {
        if (handler != null) {
            VIRTUAL_HANDLERS.add(handler);
        }
    }

    private static boolean isExternalContainer(Slot slot, AbstractContainerMenu menu) {
        for (Class<?> clazz : GLOBAL_EXTERNAL_CONTAINER_CLASSES) {
            if (clazz.isAssignableFrom(slot.container.getClass())) {
                return true;
            }
        }

        Set<Class<?>> menuSpecific = MENU_SPECIFIC_CONTAINERS.get(menu.getClass());
        if (menuSpecific != null) {
            for (Class<?> clazz : menuSpecific) {
                if (clazz.isAssignableFrom(slot.container.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

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
     * @param mode true = 全部移动；false = 只移动与背包内已有物品同类型的
     */
    public static void moveIToBackpack(MoveMode mode, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = getBackpackSlotStart(menu);
        if (start < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int end = Math.min(start + size, menu.slots.size());

        int[] invMain = findInventoryMainRange(menu, player);
        if (invMain == null) return;
        int invStart = invMain[0], invEnd = invMain[1];

        List<ItemStack> matchTypes = null;
        if (mode == MoveMode.MATCHING || mode == MoveMode.FILL_MATCHING || mode == MoveMode.ONE_EACH) {
            matchTypes = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Slot slot = menu.slots.get(i);
                if (!(slot instanceof BackpackSlot)) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) matchTypes.add(stack.copy());
            }
            if (matchTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, invStart, invEnd, start, end);
            case MATCHING -> moveMatching(menu, invStart, invEnd, start, end, matchTypes);
            case FILL_MATCHING -> fillMatching(menu, invStart, invEnd, start, end, matchTypes);
            case ONE_EACH -> moveOneEach(menu, invStart, invEnd, start, end, matchTypes);
        }
    }

    /**
     * 将背包物品移至物品栏（moveI）
     * @param mode true = 全部移动；false = 只移动与物品栏内已有物品同类型的
     */
    public static void moveBToInventory(MoveMode mode, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        int start = getBackpackSlotStart(menu);
        if (start < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int end = Math.min(start + size, menu.slots.size());

        int[] invMain = findInventoryMainRange(menu, player);
        if (invMain == null) return;
        int targetStart = invMain[0], targetEnd = invMain[1];

        List<ItemStack> matchTypes = null;
        if (mode == MoveMode.MATCHING || mode == MoveMode.FILL_MATCHING || mode == MoveMode.ONE_EACH) {
            matchTypes = new ArrayList<>();
            for (int i = targetStart; i < targetEnd; i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container != player.getInventory()) continue;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) matchTypes.add(stack.copy());
            }
            if (matchTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, start, end, targetStart, targetEnd);
            case MATCHING -> moveMatching(menu, start, end, targetStart, targetEnd, matchTypes);
            case FILL_MATCHING -> fillMatching(menu, start, end, targetStart, targetEnd, matchTypes);
            case ONE_EACH -> moveOneEach(menu, start, end, targetStart, targetEnd, matchTypes);
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

    private static void fillMatching(AbstractContainerMenu menu,
                                     int sourceStart, int sourceEnd,
                                     int targetStart, int targetEnd,
                                     List<ItemStack> matchTypes) {
        for (int i = sourceStart; i < sourceEnd; i++) {
            Slot sourceSlot = menu.slots.get(i);
            ItemStack sourceStack = sourceSlot.getItem();
            if (sourceStack.isEmpty()) continue;

            boolean typeMatches = matchTypes == null ||
                    matchTypes.stream().anyMatch(type -> ItemStack.isSameItemSameComponents(sourceStack, type));
            if (!typeMatches) continue;

            ItemStack remaining = sourceStack.copy();
            for (int j = targetStart; j < targetEnd; j++) {
                Slot targetSlot = menu.slots.get(j);
                ItemStack targetStack = targetSlot.getItem();
                if (targetStack.isEmpty() || !ItemStack.isSameItemSameComponents(remaining, targetStack)) continue;

                int maxStack = Math.min(targetSlot.getMaxStackSize(targetStack), remaining.getMaxStackSize());
                int canAdd = maxStack - targetStack.getCount();
                if (canAdd <= 0) continue;

                int toMove = Math.min(canAdd, remaining.getCount());
                if (toMove > 0) {
                    targetStack.grow(toMove);
                    remaining.shrink(toMove);
                    targetSlot.setChanged();
                    if (remaining.isEmpty()) break;
                }
            }

            if (remaining.getCount() != sourceStack.getCount()) {
                if (remaining.isEmpty()) {
                    sourceSlot.set(ItemStack.EMPTY);
                } else {
                    sourceSlot.set(remaining);
                }
                sourceSlot.setChanged();
            }
        }
    }

    private static void moveOneEach(AbstractContainerMenu menu,
                                    int sourceStart, int sourceEnd,
                                    int targetStart, int targetEnd,
                                    List<ItemStack> matchTypes) {
        if (!(menu instanceof IBackpackMenu backpackMenu)) return;

        // 去重：每种物品类型只处理一次
        List<ItemStack> uniqueTypes = new ArrayList<>();
        for (ItemStack type : matchTypes) {
            boolean exists = uniqueTypes.stream()
                    .anyMatch(unique -> ItemStack.isSameItemSameComponents(unique, type));
            if (!exists) {
                uniqueTypes.add(type.copy());
            }
        }

        for (ItemStack type : uniqueTypes) {
            int remainingToMove = type.getMaxStackSize(); // 一组大小
            if (remainingToMove <= 0) continue;

            for (int i = sourceStart; i < sourceEnd && remainingToMove > 0; i++) {
                Slot sourceSlot = menu.slots.get(i);
                ItemStack sourceStack = sourceSlot.getItem();
                if (sourceStack.isEmpty() || !ItemStack.isSameItemSameComponents(sourceStack, type)) continue;

                int countToTake = Math.min(sourceStack.getCount(), remainingToMove);
                ItemStack toMove = sourceStack.copy();
                toMove.setCount(countToTake);

                backpackMenu.yyzsbackpack$moveItemStackTo(toMove, targetStart, targetEnd, false);

                int actuallyMoved = countToTake - toMove.getCount();
                if (actuallyMoved > 0) {
                    sourceStack.shrink(actuallyMoved);
                    if (sourceStack.isEmpty()) {
                        sourceSlot.set(ItemStack.EMPTY);
                    } else {
                        sourceSlot.set(sourceStack);
                    }
                    sourceSlot.setChanged();
                    remainingToMove -= actuallyMoved;
                } else {
                    // 目标区域无法容纳，提前结束该类型的移动
                    break;
                }
            }
        }
    }

    /**
     * 将外部容器中的物品移至玩家主物品栏（9~35）
     */
    public static void moveCToInventory(MoveMode mode, ServerPlayer player) {
        Backpack.LOGGER.info("Moving C to Inventory");
        AbstractContainerMenu menu = player.containerMenu;
        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;

        int[] invMainRange = findInventoryMainRange(menu, player);
        if (invMainRange == null) return;
        int invStart = invMainRange[0];
        int invEnd = invMainRange[1];

        if (containerRange[0] == -2) {
            IVirtualContainer handler = findVirtualHandler(menu);
            if (handler == null) return;

            List<ItemStack> matchTypes = null;
            if (mode != MoveMode.ALL) {
                matchTypes = new ArrayList<>();
                for (int i = invStart; i < invEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (slot.container instanceof Inventory) {
                        ItemStack stack = slot.getItem();
                        if (!stack.isEmpty()) matchTypes.add(stack.copy());
                    }
                }
                if (matchTypes.isEmpty()) return;
            }

            boolean all = (mode == MoveMode.ALL);
            boolean moved;
            do {
                moved = handler.transfer(menu, player, 0, 0, invStart, invEnd, false, all, matchTypes);
            } while (moved);
            return;
        }

        int containerStart = containerRange[0];
        int containerEnd = containerRange[1];

        List<ItemStack> inventoryTypes = null;
        if (mode != MoveMode.ALL) {
            inventoryTypes = new ArrayList<>();
            for (int i = invStart; i < invEnd; ++i) {
                Slot slot = menu.slots.get(i);
                if (slot.container instanceof Inventory) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) inventoryTypes.add(stack.copy());
                }
            }
            if (inventoryTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, containerStart, containerEnd, invStart, invEnd);
            case MATCHING -> moveMatching(menu, containerStart, containerEnd, invStart, invEnd, inventoryTypes);
            case FILL_MATCHING -> fillMatching(menu, containerStart, containerEnd, invStart, invEnd, inventoryTypes);
            case ONE_EACH -> moveOneEach(menu, containerStart, containerEnd, invStart, invEnd, inventoryTypes);
        }
    }

    /**
     * 将玩家主物品栏（9~35）中的物品移至外部容器
     */
    public static void moveIToContainer(MoveMode mode, ServerPlayer player) {
        Backpack.LOGGER.info("Moving I to Container");
        AbstractContainerMenu menu = player.containerMenu;
        int[] invMainRange = findInventoryMainRange(menu, player);
        if (invMainRange == null) return;
        int invStart = invMainRange[0];
        int invEnd = invMainRange[1];

        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;

        if (containerRange[0] == -2) {
            IVirtualContainer handler = findVirtualHandler(menu);
            if (handler == null) return;

            List<ItemStack> matchTypes = null;
            if (mode != MoveMode.ALL) {
                matchTypes = handler.getStoredItemTypes(menu);
                if (matchTypes.isEmpty()) return;
            }

            boolean all = (mode == MoveMode.ALL);
            boolean moved;
            do {
                moved = handler.transfer(menu, player, invStart, invEnd, 0, 0, true, all, matchTypes);
            } while (moved);
            return;
        }

        int containerStart = containerRange[0];
        int containerEnd = containerRange[1];

        List<ItemStack> containerTypes = null;
        if (mode != MoveMode.ALL) {
            containerTypes = new ArrayList<>();
            for (int i = containerStart; i < containerEnd; ++i) {
                Slot slot = menu.slots.get(i);
                if (isExternalContainer(slot, menu)) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) containerTypes.add(stack.copy());
                }
            }
            if (containerTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, invStart, invEnd, containerStart, containerEnd);
            case MATCHING -> moveMatching(menu, invStart, invEnd, containerStart, containerEnd, containerTypes);
            case FILL_MATCHING -> fillMatching(menu, invStart, invEnd, containerStart, containerEnd, containerTypes);
            case ONE_EACH -> moveOneEach(menu, invStart, invEnd, containerStart, containerEnd, containerTypes);
        }
    }

    /**
     * 将外部容器中的物品移至背包
     */
    public static void moveCToBackpack(MoveMode mode, ServerPlayer player) {
        Backpack.LOGGER.info("Moving C to Backpack");
        AbstractContainerMenu menu = player.containerMenu;
        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;

        int backpackStart = getBackpackSlotStart(menu);
        if (backpackStart < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int backpackEnd = Math.min(backpackStart + size, menu.slots.size());

        if (containerRange[0] == -2) {
            IVirtualContainer handler = findVirtualHandler(menu);
            if (handler == null) return;

            List<ItemStack> matchTypes = null;
            if (mode != MoveMode.ALL) {
                matchTypes = new ArrayList<>();
                for (int i = backpackStart; i < backpackEnd; i++) {
                    Slot slot = menu.slots.get(i);
                    if (slot instanceof BackpackSlot) {
                        ItemStack stack = slot.getItem();
                        if (!stack.isEmpty()) matchTypes.add(stack.copy());
                    }
                }
                if (matchTypes.isEmpty()) return;
            }

            boolean all = (mode == MoveMode.ALL);
            boolean moved;
            do {
                moved = handler.transfer(menu, player, 0, 0, backpackStart, backpackEnd, false, all, matchTypes);
            } while (moved);
            return;
        }

        int containerStart = containerRange[0];
        int containerEnd = containerRange[1];

        List<ItemStack> backpackTypes = null;
        if (mode != MoveMode.ALL) {
            backpackTypes = new ArrayList<>();
            for (int i = backpackStart; i < backpackEnd; ++i) {
                Slot slot = menu.slots.get(i);
                if (slot instanceof BackpackSlot) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) backpackTypes.add(stack.copy());
                }
            }
            if (backpackTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, containerStart, containerEnd, backpackStart, backpackEnd);
            case MATCHING -> moveMatching(menu, containerStart, containerEnd, backpackStart, backpackEnd, backpackTypes);
            case FILL_MATCHING -> fillMatching(menu, containerStart, containerEnd, backpackStart, backpackEnd, backpackTypes);
            case ONE_EACH -> moveOneEach(menu, containerStart, containerEnd, backpackStart, backpackEnd, backpackTypes);
        }
    }

    /**
     * 将背包中的物品移至外部容器
     */
    public static void moveBToContainer(MoveMode mode, ServerPlayer player) {
        Backpack.LOGGER.info("Moving B to Container");
        AbstractContainerMenu menu = player.containerMenu;
        int backpackStart = getBackpackSlotStart(menu);
        if (backpackStart < 0) return;
        int size = BackpackSlotHelper.getBackpackSize(player);
        if (size <= 0) return;
        int backpackEnd = Math.min(backpackStart + size, menu.slots.size());

        int[] containerRange = findContainerRange(menu, player);
        if (containerRange == null) return;

        if (containerRange[0] == -2) {
            IVirtualContainer handler = findVirtualHandler(menu);
            if (handler == null) return;

            List<ItemStack> matchTypes = null;
            if (mode != MoveMode.ALL) {
                matchTypes = handler.getStoredItemTypes(menu);
                if (matchTypes.isEmpty()) return;
            }

            boolean all = (mode == MoveMode.ALL);
            boolean moved;
            do {
                moved = handler.transfer(menu, player, backpackStart, backpackEnd, 0, 0, true, all, matchTypes);
            } while (moved);
            return;
        }

        int containerStart = containerRange[0];
        int containerEnd = containerRange[1];

        List<ItemStack> containerTypes = null;
        if (mode != MoveMode.ALL) {
            containerTypes = new ArrayList<>();
            for (int i = containerStart; i < containerEnd; ++i) {
                Slot slot = menu.slots.get(i);
                if (isExternalContainer(slot, menu)) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) containerTypes.add(stack.copy());
                }
            }
            if (containerTypes.isEmpty()) return;
        }

        switch (mode) {
            case ALL -> moveAll(menu, backpackStart, backpackEnd, containerStart, containerEnd);
            case MATCHING -> moveMatching(menu, backpackStart, backpackEnd, containerStart, containerEnd, containerTypes);
            case FILL_MATCHING -> fillMatching(menu, backpackStart, backpackEnd, containerStart, containerEnd, containerTypes);
            case ONE_EACH -> moveOneEach(menu, backpackStart, backpackEnd, containerStart, containerEnd, containerTypes);
        }
    }

    /**
     * 查找是否有匹配的虚拟处理器
     */
    private static IVirtualContainer findVirtualHandler(AbstractContainerMenu menu) {
        for (IVirtualContainer h : VIRTUAL_HANDLERS) {
            if (h.matches(menu)) {
                return h;
            }
        }
        return null;
    }

    public static int[] findContainerRange(AbstractContainerMenu menu, ServerPlayer player) {
        // 先尝试真实外部容器槽位
        int start = -1;
        int end = -1;
        boolean found = false;

        for (int i = 0; i < menu.slots.size(); ++i) {
            Slot slot = menu.slots.get(i);
            if (isExternalContainer(slot, menu)) {
                if (!found) {
                    start = i;
                    found = true;
                }
                end = i;
            } else if (found) {
                break;
            }
        }

        if (start != -1) {
            return new int[]{start, end + 1};
        }

        // 没有真实槽位时，检查虚拟处理器
        if (findVirtualHandler(menu) != null) {
            // 特殊标记：-2 表示虚拟容器
            return new int[]{-2, -2};
        }

        return null;
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

    /**
     * 查找玩家快捷栏（索引 0~8）对应的菜单槽位范围
     */
    public static int[] findHotbarRange(AbstractContainerMenu menu, Player player) {
        Inventory inv = player.getInventory();
        int start = -1, end = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container == inv) {
                int idx = slot.getContainerSlot();
                if (idx >= 0 && idx < 9) {
                    if (start == -1) start = i;
                    end = i;
                }
            }
        }
        return (start == -1) ? null : new int[]{start, end + 1};
    }

    /**
     * 查找玩家完整背包（快捷栏 0-8 + 主物品栏 9-35）对应的菜单槽位范围。
     * 假设这些槽位在菜单中连续排列。
     */
    public static int[] findPlayerInventoryRange(AbstractContainerMenu menu, Player player) {
        Inventory inv = player.getInventory();
        int start = -1, end = -1;
        boolean found = false;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container == inv) {
                int idx = slot.getContainerSlot();
                if (idx >= 0 && idx < 36) {
                    if (!found) {
                        start = i;
                        found = true;
                    }
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

    /**
     * Alt+点击槽位：非背包槽位移至背包，背包槽位移至快捷栏。
     * 使用原版 Shift+点击逻辑。
     *
     * @param player    服务器玩家
     * @param slotIndex 菜单全局槽位索引
     */
    public static void quickMoveSlot(ServerPlayer player, int slotIndex) {
        AbstractContainerMenu menu = player.containerMenu;

        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return;
        }

        Slot sourceSlot = menu.slots.get(slotIndex);

        if (!sourceSlot.hasItem()) {
            return;
        }

        IBackpackMenu backpackMenu = (IBackpackMenu) menu;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        int start;
        int end;
        boolean reverse;

        if (sourceSlot instanceof BackpackSlot) {
            int[] playerRange = findPlayerInventoryRange(menu, player);
            if (playerRange == null) {
                return;
            }

            start = playerRange[0];
            end = playerRange[1];
            reverse = true;
        } else {
            int backpackStart = getBackpackSlotStart(menu);
            if (backpackStart < 0) {
                return;
            }

            int size = BackpackSlotHelper.getBackpackSize(player);
            if (size <= 0) {
                return;
            }

            start = backpackStart;
            end = Math.min(backpackStart + size, menu.slots.size());
            reverse = false;
        }

        if (!backpackMenu.yyzsbackpack$moveItemStackTo(
                sourceStack,
                start,
                end,
                reverse
        )) {
            return;
        }

        sourceSlot.setByPlayer(sourceStack);
        sourceSlot.setChanged();
        sourceSlot.onTake(player, originalStack);
    }
}