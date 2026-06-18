package com.yyz.yyzsbackpack;

public interface IExtendedInventory {

    int EXTRA_SLOT_START = 43;
    int EXTRA_SLOT_COUNT = 256;

    void yyzsbackpack$enableExtraSlots(int count);
    boolean yyzsbackpack$isExtraSlotEnabled(int index);
}
