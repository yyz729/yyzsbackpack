package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.base.BackPackSlot;
import com.yyz.yyzsbackpack.base.BackpackCondition;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin implements BackpackCondition {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract void setCarried(ItemStack stack);


    @Shadow public abstract Slot getSlot(int i);

    @Shadow protected abstract boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl);

    @Unique
    private boolean shouldRenderBackpack = false;
    @Unique
    private boolean renderTipBackpack = false;
    @Override
    public boolean shouldRenderBackpack() {
        return this.shouldRenderBackpack && !renderTipBackpack();
    }

    @Override
    public void setRenderBackpack(boolean shouldRenderBackpack) {
        this.shouldRenderBackpack = shouldRenderBackpack;
    }
    @Override
    public boolean renderTipBackpack() {
        return this.renderTipBackpack;
    }

    @Override
    public void setRenderTipBackpack(boolean renderTipBackpack) {
        this.renderTipBackpack = renderTipBackpack;
    }

    @Unique
    private int backpackXOffset = 0;
    @Unique
    private int backpackYOffset = 0;

    @Unique
    private int equippackXOffset = 0;
    @Unique
    private int equippackYOffset = 0;

    @Override
    public int getBackpackXOffset() {
        return backpackXOffset + Backpack.getConfig().backpack_offsetX;
    }

    @Override
    public int getBackpackYOffset() {
        return backpackYOffset + Backpack.getConfig().backpack_offsetY;
    }

    @Override
    public void setBackpackOffset(int x, int y) {
        this.backpackXOffset = x;
        this.backpackYOffset = y;
    }

    @Override
    public int getEquippackXOffset() {
        return equippackXOffset + Backpack.getConfig().slot_offsetX;
    }

    @Override
    public int getEquippackYOffset() {
        return equippackYOffset + Backpack.getConfig().slot_offsetY;
    }

    @Override
    public void setEquippackOffset(int x, int y) {
        this.equippackXOffset = x;
        this.equippackYOffset = y;
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {

        if (slotIndex < 0 || slotIndex >= this.slots.size() || actionType != ClickType.PICKUP || slots.get(slotIndex).getItem().getItem() instanceof BackpackItem || !Backpack.getConfig().quick_swap) return;

        if(!(getSlot(slotIndex) instanceof BackPackSlot)) return;
        ItemStack back = BackpackHelper.getEquipped(player).copy();
        ItemStack stack = getCarried().copy();
        if (!(back.getItem() instanceof BackpackItem) || !(stack.getItem() instanceof BackpackItem)) return;
        BackpackManager.saveBackpackContents(player.getInventory(), back, true);
        BackpackManager.restoreBackpackContents(player.getInventory(), stack);
        Container container = BackpackHelper.getContainer(player);
        container.setItem(BackpackHelper.getIndex(player), stack);
        setCarried(back);
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
            if (slot instanceof BackPackSlot) {
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
                if (this.slots.get(i) instanceof BackPackSlot) {
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
