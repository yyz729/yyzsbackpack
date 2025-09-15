package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackSorter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements BackpackMenu {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Unique
    private boolean isBackpackVisible = false;
    @Unique
    private boolean isPreviewVisible = false;
    @Override
    public boolean isBackpackVisible() {
        return this.isBackpackVisible && !isPreviewVisible();
    }

    @Override
    public void setBackpackVisible(boolean shouldRenderBackpack) {
        this.isBackpackVisible = shouldRenderBackpack;
    }
    @Override
    public boolean isPreviewVisible() {
        return this.isPreviewVisible;
    }

    @Override
    public void setPreviewVisible(boolean renderTipBackpack) {
        this.isPreviewVisible = renderTipBackpack;
    }

    @Unique
    private int BackpackGuiX = 0;
    @Unique
    private int BackpackGuiY = 0;

    @Unique
    private int BackpackEquipSlotX = 0;
    @Unique
    private int BackpackEquipSlotY = 0;

    @Override
    public int getBackpackGuiX() {
        return BackpackGuiX + Backpack.getConfig().backpack_gui_x;
    }

    @Override
    public int getBackpackGuiY() {
        return BackpackGuiY + Backpack.getConfig().backpack_gui_y;
    }

    @Override
    public void setBackpackGuiPos(int x, int y) {
        this.BackpackGuiX = x;
        this.BackpackGuiY = y;
    }

    @Override
    public int getBackpackEquipSlotX() {
        return BackpackEquipSlotX + Backpack.getConfig().slot_position_x;
    }

    @Override
    public int getBackpackEquipSlotY() {
        return BackpackEquipSlotY + Backpack.getConfig().slot_position_y;
    }

    @Override
    public void setBackpackEquipSlotPos(int x, int y) {
        this.BackpackEquipSlotX = x;
        this.BackpackEquipSlotY = y;
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        BackpackSorter.handleBackpackSwap((AbstractContainerMenu) (Object) this, this.slots, this.getCarried(), slotIndex, button, actionType, player);
    }

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void handleShiftRightClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {

        BackpackSorter.quickMoveTo((AbstractContainerMenu)(Object)this,slots,i,j,clickType,player,ci);

    }
    @ModifyConstant(method = "doClick", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return original + BackpackHelper.getSlotIndexOffset();
    }
}
