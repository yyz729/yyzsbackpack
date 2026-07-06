package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.MenuAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
}