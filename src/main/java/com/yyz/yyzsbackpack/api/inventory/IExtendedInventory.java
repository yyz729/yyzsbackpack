package com.yyz.yyzsbackpack.api.inventory;

import net.minecraft.world.item.ItemStack;

public interface IExtendedInventory {

    void yyzsbackpack$enableExtraSlots(int count);
    boolean yyzsbackpack$isExtraSlotEnabled(int index);

    void yyzsbackpack$syncFromBackpack(ItemStack backpack);
    void yyzsbackpack$syncToBackpack();
}
