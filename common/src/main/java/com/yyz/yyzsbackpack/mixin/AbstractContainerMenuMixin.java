package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.base.BackpackMenuState;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.BackpackSorter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerMenu.class,priority = 1001)
public abstract class AbstractContainerMenuMixin implements BackpackMenu {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Unique
    private final BackpackMenuState backpackMenuState = new BackpackMenuState();

    @Override
    public boolean isBackpackVisible() {
        return backpackMenuState.isBackpackVisible();
    }

    @Override
    public void setBackpackVisible(boolean shouldRenderBackpack) {
        backpackMenuState.setBackpackVisible(shouldRenderBackpack);
    }

    @Override
    public boolean isPreviewVisible() {
        return backpackMenuState.isPreviewVisible();
    }

    @Override
    public void setPreviewVisible(boolean renderTipBackpack) {
        backpackMenuState.setPreviewVisible(renderTipBackpack);
    }

    @Override
    public int getBackpackGuiX() {
        return backpackMenuState.getBackpackGuiX();
    }

    @Override
    public int getBackpackGuiY() {
        return backpackMenuState.getBackpackGuiY();
    }

    @Override
    public void setBackpackGuiPos(int x, int y) {
        backpackMenuState.setBackpackGuiPos(x, y);
    }

    @Override
    public int getBackpackEquipSlotX() {
        return backpackMenuState.getBackpackEquipSlotX();
    }

    @Override
    public int getBackpackEquipSlotY() {
        return backpackMenuState.getBackpackEquipSlotY();
    }

    @Override
    public void setBackpackEquipSlotPos(int x, int y) {
        backpackMenuState.setBackpackEquipSlotPos(x, y);
    }


    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        BackpackSorter.handleBackpackSwap((AbstractContainerMenu) (Object) this, this.slots, this.getCarried(), slotIndex, button, actionType, player);
    }


    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void handleShiftRightClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
        BackpackSorter.quickMoveTo((AbstractContainerMenu)(Object)this,slots,i,j,clickType,player,ci);
    }
}