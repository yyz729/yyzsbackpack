package com.yyz.yyzsbackpack.base;

public interface BackpackCondition {

    boolean shouldRenderBackpack() ;

    void setRenderBackpack(boolean bl);

    boolean renderTipBackpack() ;

    void setRenderTipBackpack(boolean bl);

    // 新增方法获取X偏移值
    int getBackpackXOffset();

    // 新增方法获取Y偏移值
    int getBackpackYOffset();

    // 新增方法设置偏移值
    void setBackpackOffset(int x, int y);

    int getEquippackXOffset();

    int getEquippackYOffset();

    void setEquippackOffset(int x, int y);
}
