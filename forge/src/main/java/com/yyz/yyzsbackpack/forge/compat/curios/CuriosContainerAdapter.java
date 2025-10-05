package com.yyz.yyzsbackpack.forge.compat.curios;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosContainerAdapter implements Container {
    private final IDynamicStackHandler stackHandler;
    private final int slotIndex;

    public CuriosContainerAdapter(IDynamicStackHandler stackHandler, int slotIndex) {
        this.stackHandler = stackHandler;
        this.slotIndex = slotIndex;
    }

    @Override
    public int getContainerSize() {
        return stackHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!stackHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return stackHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return stackHandler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return stackHandler.extractItem(index, stackHandler.getStackInSlot(index).getCount(), false);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        stackHandler.setStackInSlot(index, stack);
    }

    @Override
    public void setChanged() {
        // 不需要实现，Curios会自动处理更改
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) {
            stackHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
    // 新增方法：获取背包所在槽位的索引
    public int getBackpackSlotIndex() {
        return slotIndex;
    }
}