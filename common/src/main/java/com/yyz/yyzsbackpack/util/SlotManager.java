package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.base.BackpackEquipSlot;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class SlotManager {
    public static void repositionBackpackInventorySlots(
            AbstractContainerMenu menu,
            int backpackSlotStartIndex,
            int baseHeight,
            int xOffset,
            int yOffset
    ) {
        final int rows = 9; // 固定9行
        for (int column = 0; column < 6; column++) { // 最大6列
            for (int row = 0; row < rows; row++) {
                int slotIndex = backpackSlotStartIndex + column * rows + row;
                if (slotIndex < menu.slots.size()) {
                    Slot slot = menu.slots.get(slotIndex);
                    if(slot instanceof BackpackStorageSlot) {
                        slot.x = - 25 - column * 18 + xOffset;
                        slot.y = (baseHeight - 166) / 2 + 3 + row * 18 + yOffset;
                    }
                }
            }
        }
    }

    public static void repositionBackpackEquipSlot(
            AbstractContainerMenu menu,
            int baseHeight,
            int xOffset,
            int yOffset
    ) {
        for (Slot slot : menu.slots) {
            if (slot instanceof BackpackEquipSlot) {
                slot.x = 8 + 69 + xOffset; // 水平位置
                slot.y = (baseHeight - 166) / 2 + 8 + 18 * 2 + yOffset;
                break; // 只有一个装备槽，找到后退出
            }
        }
    }

    // 背包槽位管理
    public static void addBackpackInventorySlots(AbstractContainerMenu screenHandler, Inventory inventory) {

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                final int columnIndex = column;
                screenHandler.addSlot(new BackpackStorageSlot(screenHandler,inventory, row + (column + 1) * 9 + 27 , columnIndex, 0 , 0));
            }
        }
    }

    public static void addBackpackEquipSlot(AbstractContainerMenu screenHandler, Inventory inventory) {
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) return;
        screenHandler.addSlot(new BackpackEquipSlot(inventory, 36+54, 8 + 69 ,  8 + 18 * 2) {});
    }
}
