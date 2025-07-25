package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.base.BackpackMenu;
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

    @Shadow public abstract void setCarried(ItemStack stack);


    @Shadow public abstract Slot getSlot(int i);

    @Shadow protected abstract boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl);

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
        return BackpackGuiX + Backpack.getConfig().backpackGuiX;
    }

    @Override
    public int getBackpackGuiY() {
        return BackpackGuiY + Backpack.getConfig().backpackGuiY;
    }

    @Override
    public void setBackpackGuiPos(int x, int y) {
        this.BackpackGuiX = x;
        this.BackpackGuiY = y;
    }

    @Override
    public int getBackpackEquipSlotX() {
        return BackpackEquipSlotX + Backpack.getConfig().slotPositionX;
    }

    @Override
    public int getBackpackEquipSlotY() {
        return BackpackEquipSlotY + Backpack.getConfig().slotPositionY;
    }

    @Override
    public void setBackpackEquipSlotPos(int x, int y) {
        this.BackpackEquipSlotX = x;
        this.BackpackEquipSlotY = y;
    }
    @ModifyConstant(method = "doClick", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + 9 * 6 + 1 ;
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        BackpackSorter.handleBackpackSwap((AbstractContainerMenu) (Object) this, this.slots, this.getCarried(), slotIndex, button, actionType, player);
    }

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void handleShiftRightClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
        // 只处理 PICKUP 类型的右键点击 + Shift
        if (clickType == ClickType.QUICK_MOVE && j == 1) {
            if (i < 0) {
                ci.cancel();
                return;
            }

            Slot slot = (Slot)this.slots.get(i);
            if (!slot.mayPickup(player)) {
                ci.cancel();
                return;
            }

            // 如果是背包槽位，转移到快捷栏
            if (slot instanceof BackpackStorageSlot) {
                for (ItemStack itemStack = this.quickMoveToHotbar(player, i);
                     !itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack);
                     itemStack = this.quickMoveToHotbar(player, i)) {
                }
                ci.cancel();
                return;
            }

            // 如果是原版槽位，转移到背包
            else  { // 物品栏和快捷栏槽位
                for (ItemStack itemStack = this.quickMoveToBackpack(player, i);
                     !itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack);
                     itemStack = this.quickMoveToBackpack(player, i)) {
                }
                ci.cancel();
            }
        }
        else if (clickType == ClickType.QUICK_MOVE && j == 2) {
            Slot hoveredSlot = (Slot) this.slots.get(i);

            // 动态识别玩家物品栏槽位
            boolean isInventorySlot = false;
            if (hoveredSlot.container instanceof Inventory) {
                int slotIndexInPlayerInv = hoveredSlot.getContainerSlot();
                // 主物品栏范围：9-35 (不包括快捷栏0-8和护甲36-39)
                if (slotIndexInPlayerInv >= 9 && slotIndexInPlayerInv < 36) {
                    isInventorySlot = true;
                }
            }

            boolean isBackpackSlot = hoveredSlot instanceof BackpackStorageSlot;
            boolean isContainerSlot = !isInventorySlot && !isBackpackSlot &&
                    hoveredSlot.container != player.getInventory();

            if (!isInventorySlot && !isBackpackSlot && !isContainerSlot) return;

            if (isInventorySlot) {
                BackpackSorter.sortInventorySlots(player,this.slots);
            } else if (isBackpackSlot) {
                BackpackSorter.sortBackpackSlots(player,this.slots);
            } else {
                BackpackSorter.sortContainerSlots(player, hoveredSlot.container,this.slots);
            }
        }
    }


    @Unique
    private ItemStack quickMoveToHotbar(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);


        // 动态获取快捷栏索引范围
        int hotbarStart = -1;
        int hotbarEnd = -1;
        for (int idx = 0; idx < this.slots.size(); idx++) {
            Slot s = this.slots.get(idx);
            if (s.container instanceof Inventory &&
                    s.getContainerSlot() >= 0 &&
                    s.getContainerSlot() < 9) {
                if (hotbarStart == -1) hotbarStart = idx;
                hotbarEnd = idx + 1; // 结束索引是exclusive的
            }
        }

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // 尝试移动到快捷栏 (36-44)
            if (!this.moveItemStackTo(slotStack, hotbarStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    @Unique
    private ItemStack quickMoveToBackpack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // 查找背包槽位起始索引
            int backpackStart = -1;
            for (int i = 0; i < this.slots.size(); i++) {
                if (this.slots.get(i) instanceof BackpackStorageSlot) {
                    backpackStart = i;
                    break;
                }
            }

            if (backpackStart == -1) {
                return ItemStack.EMPTY;
            }

            // 尝试移动到背包槽位
            if (!this.moveItemStackTo(slotStack, backpackStart, backpackStart + 54, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }
}
