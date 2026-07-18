package com.yyz.yyzsbackpack.api;

import net.minecraft.world.item.ItemStack;

public interface IBackpackMenu {
    boolean yyzsbackpack$moveItemStackTo(ItemStack stack, int start, int end, boolean reverse);
}