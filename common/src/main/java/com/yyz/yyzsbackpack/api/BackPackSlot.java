package com.yyz.yyzsbackpack.api;

import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackPackSlot extends Slot {
    private final AbstractContainerMenu menu;
    private final int columnIndex;

    public BackPackSlot(AbstractContainerMenu menu, Container container, int index, int columnIndex, int j, int k) {
        super(container, index, j, k);
        this.menu = menu;
        this.columnIndex = columnIndex;
    }

    @Override
    public boolean isActive() {
        ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return false;
        }

        int columns = backpackItem.getBackpackType().getColumns();
        if (columnIndex >= columns) {
            return false;
        }

        if(!(((BackpackCondition)menu).shouldRenderBackpack())){
            return false;
        }

        return true;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
        boolean canPlace = !(stack.getItem() instanceof BackpackItem) &&
                backpackStack.getItem() instanceof BackpackItem &&
                super.mayPlace(stack);

        // 添加列数检查（仅添加这一部分）
        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
            int columns = backpackItem.getBackpackType().getColumns();
            if (columnIndex >= columns) {
                return false;
            }
        }

        return canPlace;
    }
}
