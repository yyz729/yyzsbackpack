package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackEquipSlot;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotManager {

    public static void repositionBackpackInventorySlots(
            AbstractContainerMenu menu,
            Inventory inventory, int backpackSlotStartIndex,
            int baseHeight,
            int xOffset,
            int yOffset
    ) {
        ItemStack stack = BackpackPlatform.getEquipped(inventory.player);
        if(stack.getItem() instanceof BackpackItem backpackItem) {

            int columns = backpackItem.getBackpackType().getColumns();
            int rows = backpackItem.getBackpackType().getRows();
            for (int row = 0; row < rows; row++) {  // 外层循环行
                for (int column = 0; column < columns; column++) {  // 内层循环列
                    // 计算槽位索引：行优先顺序 (row * columns + column)
                    int slotIndex = backpackSlotStartIndex + row * columns + column;
                    if (slotIndex < menu.slots.size()) {
                        Slot slot = menu.slots.get(slotIndex);
                        if (slot instanceof BackpackStorageSlot) {
                            // X坐标：从左到右递增（列增加）
                            // Y坐标：从上到下递增（行增加）
                            slot.x = -25 -(columns-1)*18+ column * 18 + xOffset;
                            slot.y = (baseHeight - 166) / 2 + 3 + row * 18 + yOffset;
                        }
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

        for (int count = 0; count < BackpackHelper.getMaxBackpackSize(); count++){
            screenHandler.addSlot(new BackpackStorageSlot(screenHandler,inventory, 36 + count ,  0 , 0));
        }
//        for (int column = 0; column < 6; column++) {
//            for (int row = 0; row < 9; row++) {
//                final int columnIndex = column;
//                screenHandler.addSlot(new BackpackStorageSlot(screenHandler,inventory, row + (column + 1) * 9 + 27 , columnIndex, 0 , 0));
//            }
//        }
    }

    public static void addBackpackEquipSlot(AbstractContainerMenu screenHandler, Container inventory) {
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) return;
        screenHandler.addSlot(new BackpackEquipSlot(inventory, 36 + BackpackHelper.getMaxBackpackSize(), 8 + 69 ,  8 + 18 * 2));
    }

    public static void updateBackpackSlotPositions(Screen screen, AbstractContainerMenu menu, Inventory inventory, int imageHeight) {


        // 跳过创造模式界面
        if (screen instanceof CreativeModeInventoryScreen) {
            return;
        }

        if (menu instanceof BackpackMenu) {
            int baseHeight = imageHeight;

            // 查找第一个背包存储槽位的索引
            int backpackSlotStartIndex = -1;
            for (int i = 0; i < menu.slots.size(); i++) {
                if (menu.slots.get(i) instanceof BackpackStorageSlot) {
                    backpackSlotStartIndex = i;
                    break;
                }
            }

            // 未找到有效槽位则终止操作
            if (backpackSlotStartIndex == -1) {
                return;
            }

            // 获取背包槽位偏移量
            int xOffset = ((BackpackMenu) menu).getBackpackGuiX();
            int yOffset = ((BackpackMenu) menu).getBackpackGuiY();

            // 获取装备槽位偏移量
            int equipXOffset = ((BackpackMenu) menu).getBackpackEquipSlotX();
            int equipYOffset = ((BackpackMenu) menu).getBackpackEquipSlotY();

            // 更新背包物品槽位位置
            SlotManager.repositionBackpackInventorySlots(
                    menu,
                    inventory,
                    backpackSlotStartIndex,
                    baseHeight,
                    xOffset,
                    yOffset
            );

            // 更新装备槽位位置
            SlotManager.repositionBackpackEquipSlot(
                    menu,
                    baseHeight,
                    equipXOffset,
                    equipYOffset
            );
        }
    }

}
