package com.yyz.yyzsbackpack.api;

import net.minecraft.world.item.ItemStack;

public interface IBackpackData {
    /** 获取同步来的背包堆（客户端可用） */
    ItemStack yyzsbackpack$getSyncedBackpack();

    /** 设置同步来的背包堆（仅服务端调用） */
    void yyzsbackpack$setSyncedBackpack(ItemStack stack);

    int yyzsbackpack$getSyncedBackpackIndex();
    void yyzsbackpack$setSyncedBackpackIndex(int index);
}