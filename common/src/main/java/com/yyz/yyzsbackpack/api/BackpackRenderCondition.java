package com.yyz.yyzsbackpack.api;

import net.minecraft.world.entity.player.Inventory;

public interface BackpackRenderCondition {

    boolean shouldRenderBackpack() ;

    void setRenderBackpack(boolean bl);

    // 新增方法获取X偏移值
    int getBackpackXOffset();

    // 新增方法获取Y偏移值
    int getBackpackYOffset();

    // 新增方法设置偏移值
    void setBackpackOffset(int x, int y);

    int getEquippackXOffset();

    int getEquippackYOffset();

    void setEquippackOffset(int x, int y);

    Inventory getInventory();

    void setInventory(Inventory inventory);
}
