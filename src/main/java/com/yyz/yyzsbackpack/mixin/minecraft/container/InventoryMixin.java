package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements IExtendedInventory {

    @Shadow
    public abstract int getContainerSize();

    @Shadow
    public abstract ItemStack getItem(int slot);

    @Unique
    private static final int EXTRA_SLOT_COUNT = 256;

    @Unique
    private final NonNullList<ItemStack> extraItems = NonNullList.withSize(EXTRA_SLOT_COUNT, ItemStack.EMPTY);

    @Unique
    private final boolean[] extraSlotEnabled = new boolean[EXTRA_SLOT_COUNT];

    @Override
    public void yyzsbackpack$enableExtraSlots(int count) {
        int enabled = Math.min(count, EXTRA_SLOT_COUNT);
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            extraSlotEnabled[i] = (i < enabled);
        }
        for (int i = enabled; i < EXTRA_SLOT_COUNT; i++) {
            extraItems.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean yyzsbackpack$isExtraSlotEnabled(int index) {
        return index >= 0 && index < EXTRA_SLOT_COUNT && extraSlotEnabled[index];
    }

    @Override
    public void yyzsbackpack$syncFromBackpack(ItemStack backpack) {
        // 清空并禁用所有额外槽位
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            extraSlotEnabled[i] = false;
            extraItems.set(i, ItemStack.EMPTY);
        }

        if (backpack.getItem() instanceof BackpackItem backpackItem) {
            BackpackData data = backpackItem.getData();
            if (data != null) {
                int size = Math.min(data.size(), EXTRA_SLOT_COUNT);
                yyzsbackpack$enableExtraSlots(size);

                // 从物品 NBT 读取 "Items" 列表
                CompoundTag tag = backpack.getTag();
                if (tag != null) {
                    NonNullList<ItemStack> temp = NonNullList.withSize(size, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(tag, temp);
                    for (int i = 0; i < size; i++) {
                        extraItems.set(i, temp.get(i));
                    }
                }
            }
        }
    }

    @Override
    public void yyzsbackpack$syncToBackpack() {
        Inventory inv = (Inventory) (Object) this;
        Player player = inv.player;
        if (player == null) return;

        ItemStack equippedBackpack = BackpackSlotHelper.getSelectedBackpack(player);
        if (!(equippedBackpack.getItem() instanceof BackpackItem)) return;

        // 写入物品 NBT
        CompoundTag tag = equippedBackpack.getOrCreateTag();
        NonNullList<ItemStack> contentsList = NonNullList.withSize(EXTRA_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            if (extraSlotEnabled[i]) {
                contentsList.set(i, extraItems.get(i).copy());
            }
        }
        ContainerHelper.saveAllItems(tag, contentsList);
    }

    @Override
    public void yyzsbackpack$switchToBackpack(int newIndex) {
        Inventory inv = (Inventory) (Object) this;
        Player player = inv.player;
        if (player == null) return;

        yyzsbackpack$syncToBackpack();
        BackpackSlotHelper.setSelectedIndex(player, newIndex);
        yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));
        inv.setChanged();
    }

    @Unique
    private int getExtraIndex(int slot) {
        return slot - getContainerSize();
    }

    @Unique
    private boolean isExtraSlot(int slot) {
        return slot >= getContainerSize() && slot < getContainerSize() + EXTRA_SLOT_COUNT;
    }

    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private void onGetItem(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            cir.setReturnValue(extraItems.get(getExtraIndex(slot)));
        }
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    private void onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (isExtraSlot(slot)) {
            extraItems.set(getExtraIndex(slot), stack);
            yyzsbackpack$syncToBackpack();
            ci.cancel();
        }
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onRemoveItem(int slot, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            cir.setReturnValue(ContainerHelper.removeItem(extraItems, getExtraIndex(slot), count));
            yyzsbackpack$syncToBackpack();
        }
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true)
    private void onRemoveItemNoUpdate(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            ItemStack stack = extraItems.get(getExtraIndex(slot));
            extraItems.set(getExtraIndex(slot), ItemStack.EMPTY);
            cir.setReturnValue(stack);
            yyzsbackpack$syncToBackpack();
        }
    }

    @ModifyReturnValue(method = "isEmpty", at = @At("RETURN"))
    private boolean modifyIsEmpty(boolean original) {
        if (!original) return false;
        for (ItemStack stack : extraItems) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @ModifyReturnValue(method = "getFreeSlot", at = @At("RETURN"))
    private int modifyGetFreeSlot(int original) {
        if (original != -1) return original;
        for (int i = 0; i < extraItems.size(); i++) {
            if (extraItems.get(i).isEmpty() && extraSlotEnabled[i]) {
                return getContainerSize() + i;
            }
        }
        return -1;
    }

    @ModifyReturnValue(method = "getSlotWithRemainingSpace", at = @At("RETURN"))
    private int modifyGetSlotWithRemainingSpace(int original, ItemStack stack) {
        if (original != -1) return original;
        for (int i = 0; i < extraItems.size(); i++) {
            ItemStack existing = extraItems.get(i);
            if (extraSlotEnabled[i] && !existing.isEmpty()
                    && ItemStack.isSameItemSameTags(existing, stack)
                    && existing.isStackable()
                    && existing.getCount() < ((Inventory) (Object) this).getMaxStackSize()) {
                return getContainerSize() + i;
            }
        }
        return -1;
    }

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void onFillStackedContents(StackedContents contents, CallbackInfo ci) {
        for (ItemStack stack : extraItems) {
            contents.accountSimpleStack(stack);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void onLoad(ListTag listTag, CallbackInfo ci) {
        Inventory inv = (Inventory) (Object) this;
        ItemStack back = BackpackSlotHelper.getSelectedBackpack(inv.player);
        if (back.getItem() instanceof BackpackItem) {
            yyzsbackpack$syncFromBackpack(back);
        } else {
            for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
                extraSlotEnabled[i] = false;
                extraItems.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Inventory inv = (Inventory) (Object) this;
        if (inv.player != null && !inv.player.level().isClientSide()) {
            cir.setReturnValue(addInternal(-1, stack));
            yyzsbackpack$syncToBackpack();
        }
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAddWithSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Inventory inv = (Inventory) (Object) this;
        if (inv.player != null && !inv.player.level().isClientSide()) {
            cir.setReturnValue(addInternal(slot, stack));
            yyzsbackpack$syncToBackpack();
        }
    }

    @Unique
    private boolean addInternal(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Inventory inv = (Inventory) (Object) this;

        if (stack.isDamaged()) {
            if (slot == -1) slot = inv.getFreeSlot();
            if (slot >= 0) {
                inv.setItem(slot, stack.copyAndClear());
                inv.setChanged();
                return true;
            }
            return inv.player.getAbilities().instabuild;
        }

        int remaining = stack.getCount();
        if (slot == -1) {
            int target = inv.getSlotWithRemainingSpace(stack);
            while (target != -1 && remaining > 0) {
                ItemStack targetStack = inv.getItem(target);
                int maxAdd = inv.getMaxStackSize() - targetStack.getCount();
                int add = Math.min(remaining, maxAdd);
                targetStack.grow(add);
                remaining -= add;
                targetStack.setPopTime(5);
                inv.setChanged();
                target = inv.getSlotWithRemainingSpace(stack);
            }
            if (remaining > 0) {
                int free = inv.getFreeSlot();
                if (free != -1) {
                    inv.setItem(free, stack.copyWithCount(remaining));
                    remaining = 0;
                    inv.setChanged();
                }
            }
        } else {
            if (isExtraSlot(slot) && !extraSlotEnabled[getExtraIndex(slot)]) {
                return false;
            }
            ItemStack target = inv.getItem(slot);
            if (target.isEmpty()) {
                inv.setItem(slot, stack.copyWithCount(remaining));
                remaining = 0;
                inv.setChanged();
            } else if (ItemStack.isSameItemSameTags(target, stack) && target.isStackable()) {
                int maxAdd = inv.getMaxStackSize() - target.getCount();
                int add = Math.min(remaining, maxAdd);
                target.grow(add);
                remaining -= add;
                target.setPopTime(5);
                inv.setChanged();
            }
        }

        if (remaining == 0) {
            stack.setCount(0);
            return true;
        }
        stack.setCount(remaining);
        return inv.player.getAbilities().instabuild;
    }

    @Inject(method = "setItem", at = @At("RETURN"))
    private void onSetItemReturn(int slot, ItemStack stack, CallbackInfo ci) {
        Inventory inv = (Inventory) (Object) this;
        yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(inv.player));
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void onRemoveItemReturn(int slot, int count, CallbackInfoReturnable<ItemStack> cir) {
        Inventory inv = (Inventory) (Object) this;
        yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(inv.player));
    }

    @Inject(method = "removeItemNoUpdate", at = @At("RETURN"))
    private void onRemoveItemNoUpdateReturn(int slot, CallbackInfoReturnable<ItemStack> cir) {
        Inventory inv = (Inventory) (Object) this;
        yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(inv.player));
    }

    @Inject(method = "clearContent", at = @At("RETURN"))
    private void onClearContentReturn(CallbackInfo ci) {
        Inventory inv = (Inventory) (Object) this;
        yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(inv.player));
    }

    @Inject(method = "setChanged", at = @At("HEAD"))
    private void onSetChanged(CallbackInfo ci) {
        Inventory inv = (Inventory) (Object) this;
        if (inv.player != null) {
            for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
                if (extraSlotEnabled[i]) {
                    yyzsbackpack$syncToBackpack();
                    break;
                }
            }
        }
    }

    @Inject(method = "findSlotMatchingItem", at = @At("RETURN"), cancellable = true)
    private void onFindSlotMatchingItem(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) {
            return;
        }

        for (int i = 0; i < extraItems.size(); i++) {
            if (extraSlotEnabled[i] && !extraItems.get(i).isEmpty()
                    && ItemStack.isSameItemSameTags(stack, extraItems.get(i))) {
                cir.setReturnValue(getContainerSize() + i);
                return;
            }
        }
    }

    @Inject(method = "findSlotMatchingUnusedItem", at = @At("RETURN"), cancellable = true)
    private void onFindSlotMatchingUnusedItem(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) {
            return;
        }

        for (int i = 0; i < extraItems.size(); i++) {
            if (extraSlotEnabled[i] && !extraItems.get(i).isEmpty()
                    && ItemStack.isSameItemSameTags(stack, extraItems.get(i))
                    && !extraItems.get(i).isDamaged()
                    && !extraItems.get(i).isEnchanted()
                    && !extraItems.get(i).hasCustomHoverName()) {
                cir.setReturnValue(getContainerSize() + i);
                return;
            }
        }
    }

    @Inject(method = "pickSlot", at = @At("HEAD"), cancellable = true)
    private void onPickSlot(int slot, CallbackInfo ci) {
        Inventory inv = (Inventory)(Object)this;

        int selected = inv.getSuitableHotbarSlot();
        inv.selected = selected; // 1.21.1 中 selected 是 public 字段

        ItemStack selectedStack = inv.getItem(selected);
        ItemStack targetStack = inv.getItem(slot);

        inv.setItem(selected, targetStack);
        inv.setItem(slot, selectedStack);

        inv.setChanged();
        yyzsbackpack$syncToBackpack();
        ci.cancel();
    }

    @Inject(method = "setPickedItem", at = @At("HEAD"), cancellable = true)
    private void onSetPickedItem(ItemStack stack, CallbackInfo ci) {
        Inventory inv = (Inventory)(Object)this;

        int i = inv.findSlotMatchingItem(stack);
        if (Inventory.isHotbarSlot(i)) {
            inv.selected = i;
        } else if (i == -1) {
            inv.selected = inv.getSuitableHotbarSlot();
            if (!inv.getItem(inv.selected).isEmpty()) {
                int j = inv.getFreeSlot();
                if (j != -1) {
                    inv.setItem(j, inv.getItem(inv.selected));
                }
            }
            inv.setItem(inv.selected, stack);
        } else {
            inv.pickSlot(i);
        }

        inv.setChanged();
        yyzsbackpack$syncToBackpack();
        ci.cancel();
    }
}