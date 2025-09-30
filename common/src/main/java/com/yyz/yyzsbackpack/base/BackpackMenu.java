package com.yyz.yyzsbackpack.base;

public interface BackpackMenu {

    boolean isBackpackVisible() ;

    void setBackpackVisible(boolean bl);

    boolean isPreviewVisible() ;

    void setPreviewVisible(boolean bl);

    // 新增方法获取X偏移值
    int getBackpackGuiX();

    // 新增方法获取Y偏移值
    int getBackpackGuiY();

    // 新增方法设置偏移值
    void setBackpackGuiPos(int x, int y);

    int getBackpackEquipSlotX();

    int getBackpackEquipSlotY();

    void setBackpackEquipSlotPos(int x, int y);
}
