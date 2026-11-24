package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class BackpackInsert {
    private static final int MAX_EXTRA_SLOTS = 256;

    private BackpackInsert() {}

    public static ItemStack insert(Inventory inv, ItemStack stack) {
        if (stack.isEmpty() || !canPlace(stack)) return stack;

        IExtendedInventory ext = (IExtendedInventory) inv;
        ext.yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(inv.player));

        int first = inv.getContainerSize();
        ItemStack remaining = stack.copy();

        for (int i = 0; i < MAX_EXTRA_SLOTS && !remaining.isEmpty(); i++) {
            if (!ext.yyzsbackpack$isExtraSlotEnabled(i)) break;
            ItemStack existing = inv.getItem(first + i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) continue;

            int move = Math.min(maxStack(existing) - existing.getCount(), remaining.getCount());
            if (move <= 0) continue;

            ItemStack grown = existing.copy();
            grown.grow(move);
            inv.setItem(first + i, grown);
            remaining.shrink(move);
        }

        for (int i = 0; i < MAX_EXTRA_SLOTS && !remaining.isEmpty(); i++) {
            if (!ext.yyzsbackpack$isExtraSlotEnabled(i)) break;
            if (!inv.getItem(first + i).isEmpty()) continue;

            int move = Math.min(maxStack(remaining), remaining.getCount());
            inv.setItem(first + i, remaining.copyWithCount(move));
            remaining.shrink(move);
        }

        return remaining;
    }

    private static boolean canPlace(ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem);
    }

    private static int maxStack(ItemStack stack) {
        return stack.getMaxStackSize();
    }
}