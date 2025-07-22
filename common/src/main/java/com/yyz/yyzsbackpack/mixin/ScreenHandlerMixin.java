package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.base.BackPackSlot;
import com.yyz.yyzsbackpack.base.BackpackCondition;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = AbstractContainerMenu.class,priority = 999)
public abstract class ScreenHandlerMixin implements BackpackCondition {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract void setCarried(ItemStack stack);

    @Shadow public abstract Slot getSlot(int i);

    @Shadow protected abstract boolean moveItemStackTo(ItemStack arg, int m, int n, boolean bl);

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

    @Inject(method = "addStandardInventorySlots", at = @At("RETURN"))
    private void addSlot(Container container, int i, int j, CallbackInfo ci) {
        if(container instanceof Inventory inventory) {
            setRenderBackpack(true);
            AbstractContainerMenu containerMenu = (AbstractContainerMenu) (Object) this;
            BackpackManager.addBackpackSlots(containerMenu, inventory);
        }
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

            boolean isBackpackSlot = hoveredSlot instanceof BackPackSlot;

            if (!isInventorySlot && !isBackpackSlot) return;

            if (isInventorySlot) {
                sortInventorySlots(player);
            } else {
                sortBackpackSlots(player);
            }
        }
    }
    @ModifyConstant(method = "doClick", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + 9 * 6 + 1 ;
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

    @Unique
    private void sortInventorySlots(Player player) {
        List<Slot> inventorySlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            // 只处理玩家物品栏中的主物品栏槽位 (9-35)
            if (slot.container instanceof Inventory) {
                int index = slot.getContainerSlot();
                if (index >= 9 && index < 36) {
                    inventorySlots.add(slot);
                }
            }
        }
        sortSlots(inventorySlots);
    }

    // 背包槽位排序保持不变
    @Unique
    private void sortBackpackSlots(Player player) {
        List<Slot> backpackSlots = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot instanceof BackPackSlot) {
                backpackSlots.add(slot);
            }
        }
        sortSlots(backpackSlots);
    }

    @Unique
    private void sortSlots(List<Slot> slotsToSort) {
        // 1. 收集所有非空物品
        List<ItemStack> items = new ArrayList<>();
        for (Slot slot : slotsToSort) {
            if (!slot.getItem().isEmpty()) {
                items.add(slot.getItem().copy());
                slot.set(ItemStack.EMPTY); // 清空槽位
            }
        }

        // 2. 按类型分组并排序
        Map<ResourceLocation, List<ItemStack>> groupedItems = new HashMap<>();
        for (ItemStack stack : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            groupedItems.computeIfAbsent(id, k -> new ArrayList<>()).add(stack);
        }

        // 按物品ID排序
        List<ResourceLocation> sortedIds = new ArrayList<>(groupedItems.keySet());
        Collections.sort(sortedIds);

        // 3. 重新填充槽位（堆叠相同物品）
        int slotIndex = 0;
        for (ResourceLocation id : sortedIds) {
            List<ItemStack> stacks = groupedItems.get(id);

            // 合并相同物品
            List<ItemStack> merged = mergeStacks(stacks);

            for (ItemStack stack : merged) {
                if (slotIndex >= slotsToSort.size()) break;
                slotsToSort.get(slotIndex++).set(stack);
            }
        }
    }

    @Unique
    private List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        if (stacks.isEmpty()) return merged;

        ItemStack current = stacks.get(0).copy();

        for (int i = 1; i < stacks.size(); i++) {
            ItemStack next = stacks.get(i);

            // 如果相同且可堆叠
            if (ItemStack.isSameItemSameComponents(current, next)) {
                int space = current.getMaxStackSize() - current.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, next.getCount());
                    current.grow(transfer);
                    next.shrink(transfer);
                }

                if (next.getCount() <= 0) continue;
            }

            // 当前堆栈已满，添加到结果
            merged.add(current);
            current = next.copy();
        }

        merged.add(current);
        return merged;
    }
}
