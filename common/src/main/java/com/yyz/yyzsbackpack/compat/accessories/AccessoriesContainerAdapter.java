package com.yyz.yyzsbackpack.compat.accessories;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AccessoriesContainerAdapter implements Container {
    private final Container container;
    private final int slotIndex;

    public AccessoriesContainerAdapter(Container container, int slotIndex) {
        this.container = container;
        this.slotIndex = slotIndex;
    }

    public int getBackpackSlotIndex() {
        return slotIndex;
    }

    @Override
    public int getContainerSize() {
        return container.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return container.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return container.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return container.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        container.setItem(index, stack);
    }

    @Override
    public void setChanged() {
        container.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void clearContent() {
        container.clearContent();
    }
}