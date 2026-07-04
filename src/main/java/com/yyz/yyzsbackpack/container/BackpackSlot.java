package com.yyz.yyzsbackpack.container;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackSlot extends Slot {
    private final Inventory inventory;
    private final int slotIndex;
    private final int EXTRA_SLOT_START;
    private boolean active;

    public BackpackSlot(Inventory inventory, int slotIndex, int x, int y, int slotStart) {
        super(inventory, slotIndex, x, y);
        this.inventory = inventory;
        this.slotIndex = slotIndex;
        this.EXTRA_SLOT_START = slotStart;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        IExtendedInventory ext = (IExtendedInventory) inventory;

        int extraIndex = slotIndex - EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex) && super.mayPlace(stack);
    }

    @Override
    public boolean isActive() {
        IExtendedInventory ext = (IExtendedInventory) inventory;
        int extraIndex = slotIndex - EXTRA_SLOT_START;
        return ext.yyzsbackpack$isExtraSlotEnabled(extraIndex);
    }

}