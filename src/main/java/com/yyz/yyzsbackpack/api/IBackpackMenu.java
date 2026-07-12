package com.yyz.yyzsbackpack.api;

import net.minecraft.world.item.ItemStack;

public interface IBackpackMenu {
    int yyzsbackpack$getBackpackSlotStart();
    void yyzsbackpack$setBackpackSlotStart(int start);

    boolean yyzsbackpack$moveItemStackTo(ItemStack stack, int start, int end, boolean reverse);
}