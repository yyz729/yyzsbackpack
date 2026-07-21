package com.yyz.yyzsbackpack.inventory;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackSlot extends Slot {
    private final Inventory inventory;
    private final int slotIndex;
    private final int EXTRA_SLOT_START;

    public BackpackSlot(Inventory inventory, int slotIndex, int x, int y, int slotStart) {
        super(inventory, slotIndex, x, y);
        this.inventory = inventory;
        this.slotIndex = slotIndex;
        this.EXTRA_SLOT_START = slotStart;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        IExtendedInventory ext = (IExtendedInventory) inventory;

        int extraIndex = slotIndex - EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex) && super.mayPlace(stack) && stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public boolean isActive() {
        IExtendedInventory ext = (IExtendedInventory) inventory;
        int extraIndex = slotIndex - EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex);
    }

}