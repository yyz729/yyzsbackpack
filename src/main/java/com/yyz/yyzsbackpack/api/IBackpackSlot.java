package com.yyz.yyzsbackpack.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IBackpackSlot {
    ItemStack getStack();
    void setStack(ItemStack stack);
    default Component getDisplayName() {
        return getStack().getHoverName();
    }
}