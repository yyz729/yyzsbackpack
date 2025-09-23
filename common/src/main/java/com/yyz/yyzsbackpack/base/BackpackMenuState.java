package com.yyz.yyzsbackpack.base;

import com.yyz.yyzsbackpack.Backpack;

public class BackpackMenuState {
    private boolean isBackpackVisible = false;
    private boolean isPreviewVisible = false;

    private int backpackGuiX = 0;
    private int backpackGuiY = 0;
    private int backpackEquipSlotX = 0;
    private int backpackEquipSlotY = 0;

    public boolean isBackpackVisible() {
        return this.isBackpackVisible && !isPreviewVisible();
    }

    public void setBackpackVisible(boolean shouldRenderBackpack) {
        this.isBackpackVisible = shouldRenderBackpack;
    }

    public boolean isPreviewVisible() {
        return this.isPreviewVisible;
    }

    public void setPreviewVisible(boolean renderTipBackpack) {
        this.isPreviewVisible = renderTipBackpack;
    }

    public int getBackpackGuiX() {
        return backpackGuiX + Backpack.getConfig().backpack_gui_x;
    }

    public int getBackpackGuiY() {
        return backpackGuiY + Backpack.getConfig().backpack_gui_y;
    }

    public void setBackpackGuiPos(int x, int y) {
        this.backpackGuiX = x;
        this.backpackGuiY = y;
    }

    public int getBackpackEquipSlotX() {
        return backpackEquipSlotX + Backpack.getConfig().slot_position_x;
    }

    public int getBackpackEquipSlotY() {
        return backpackEquipSlotY + Backpack.getConfig().slot_position_y;
    }

    public void setBackpackEquipSlotPos(int x, int y) {
        this.backpackEquipSlotX = x;
        this.backpackEquipSlotY = y;
    }
}