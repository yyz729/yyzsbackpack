package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BackpackStorage {
    // 保存背包内容到数据组件
    public static void saveBackpackContents(Container inventory, ItemStack backpackStack, boolean b) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        // 创建固定大小的列表（所有槽位，包括空）
        List<ItemStack> items = new ArrayList<>(numSlots);
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 36 + i;
            ItemStack stack = inventory.getItem(slotIndex);
            // 复制堆栈防止引用问题
            items.add(stack.copy());
            // 清空原库存槽位
            if(b) {
                inventory.setItem(slotIndex, ItemStack.EMPTY);
            }
        }

        // 设置数据组件
        backpackStack.set(BackpackPlatform.getBackpackItemsComponent(), items);
    }

    // 从数据组件恢复背包内容
    public static void restoreBackpackContents(Container inventory, ItemStack backpackStack) {
        // 获取数据组件

        List<ItemStack> items = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());
        if (items == null) return;

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        // 恢复物品到对应槽位
        for (int i = 0; i < Math.min(items.size(), numSlots); i++) {
            ItemStack stack = items.get(i);
            // 只恢复非空堆栈
            if (!stack.isEmpty()) {
                inventory.setItem(36 + i, stack.copy());
            }
        }

        // 移除数据组件
        backpackStack.remove(BackpackPlatform.getBackpackItemsComponent());
    }


    public static int countNonEmptyBackpacks(Container playerInventory) {
        int nonEmptyBackpackCount = 0;

        // 遍历玩家物品栏所有槽位（通常0-35是主物品栏+快捷栏）
        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            ItemStack stack = playerInventory.getItem(slot);

            // 检查是否是背包物品
            if (stack.getItem() instanceof BackpackItem) {
                // 获取背包的存储数据组件
                List<ItemStack> backpackItems = stack.get(BackpackPlatform.getBackpackItemsComponent());

                // 检查背包是否有存储内容
                if (backpackItems != null) {
                    // 遍历背包内所有物品槽位
                    for (ItemStack content : backpackItems) {
                        if (!content.isEmpty()) {
                            nonEmptyBackpackCount++; // 发现非空物品，计数+1
                            break; // 跳出内部循环，继续检查下一个背包
                        }
                    }
                }
            }
        }
        return nonEmptyBackpackCount;
    }
}
