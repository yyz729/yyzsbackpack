package com.yyz.yyzsbackpack;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackSlot extends Slot {
    private final Inventory inventory;
    private final int slotIndex;

    public BackpackSlot(Inventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.inventory = inventory;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        IExtendedInventory ext = (IExtendedInventory) inventory;
        // 使用接口常量
        int extraIndex = slotIndex - IExtendedInventory.EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex) && super.mayPlace(stack);
    }

    @Override
    public boolean isActive() {
        IExtendedInventory ext = (IExtendedInventory) inventory;
        int extraIndex = slotIndex - IExtendedInventory.EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex);
    }
}