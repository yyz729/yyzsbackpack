package com.yyz.yyzsbackpack.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yyz.yyzsbackpack.IExtendedInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin implements IExtendedInventory {



    @Unique
    private final NonNullList<ItemStack> extraItems = NonNullList.withSize(EXTRA_SLOT_COUNT, ItemStack.EMPTY);

    @Unique private final boolean[] extraSlotEnabled = new boolean[EXTRA_SLOT_COUNT];

    @Override
    public void yyzsbackpack$enableExtraSlots(int count) {
        int enabled = Math.min(count, EXTRA_SLOT_COUNT);
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            extraSlotEnabled[i] = (i < enabled);
        }
        // 清空被禁用的槽位
        for (int i = enabled; i < EXTRA_SLOT_COUNT; i++) {
            extraItems.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean yyzsbackpack$isExtraSlotEnabled(int index) {
        return index >= 0 && index < EXTRA_SLOT_COUNT && extraSlotEnabled[index];
    }

    @Unique
    private int getExtraIndex(int slot) {
        return slot - EXTRA_SLOT_START;
    }

    @Unique
    private boolean isExtraSlot(int slot) {
        return slot >= EXTRA_SLOT_START && slot < EXTRA_SLOT_START + EXTRA_SLOT_COUNT;
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
            ci.cancel();
        }
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onRemoveItem(int slot, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            cir.setReturnValue(ContainerHelper.removeItem(extraItems, getExtraIndex(slot), count));
        }
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true)
    private void onRemoveItemNoUpdate(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            ItemStack stack = extraItems.get(getExtraIndex(slot));
            extraItems.set(getExtraIndex(slot), ItemStack.EMPTY);
            cir.setReturnValue(stack);
        }
    }

    @ModifyReturnValue(method = "getContainerSize", at = @At("RETURN"))
    private int modifyContainerSize(int original) {
        return original + EXTRA_SLOT_COUNT;
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
        if (original != -1) return original; // 主物品栏有空位
        // 查找额外空位
        for (int i = 0; i < extraItems.size(); i++) {
            if (extraItems.get(i).isEmpty() && extraSlotEnabled[i]) {
                return EXTRA_SLOT_START + i;
            }
        }
        return -1;
    }

    @ModifyReturnValue(method = "getSlotWithRemainingSpace", at = @At("RETURN"))
    private int modifyGetSlotWithRemainingSpace(int original, ItemStack stack) {
        if (original != -1) return original; // 主物品栏有可堆叠槽
        // 查找额外可堆叠槽
        for (int i = 0; i < extraItems.size(); i++) {
            ItemStack existing = extraItems.get(i);
            if (extraSlotEnabled[i] && !existing.isEmpty()
                    && ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.isStackable()
                    && existing.getCount() < ((Inventory)(Object)this).getMaxStackSize(existing)) {
                return EXTRA_SLOT_START + i;
            }
        }
        return -1;
    }

    @Inject(method = "clearContent", at = @At("HEAD"))
    private void onClearContent(CallbackInfo ci) {
        extraItems.clear();
    }

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void onFillStackedContents(StackedItemContents contents, CallbackInfo ci) {
        for (ItemStack stack : extraItems) {
            contents.accountSimpleStack(stack);
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void onSave(ValueOutput.TypedOutputList<ItemStackWithSlot> output, CallbackInfo ci) {
        for (int i = 0; i < extraItems.size(); i++) {
            ItemStack stack = extraItems.get(i);
            if (!stack.isEmpty()) {
                output.add(new ItemStackWithSlot(EXTRA_SLOT_START + i, stack));
            }
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void onLoad(ValueInput.TypedInputList<ItemStackWithSlot> input, CallbackInfo ci) {
        // 先清空额外列表（原方法不会动它）
        extraItems.clear();
        for (net.minecraft.world.ItemStackWithSlot entry : input) {
            int slot = entry.slot();
            if (isExtraSlot(slot)) {
                extraItems.set(getExtraIndex(slot), entry.stack());
            }
        }
    }

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(addInternal(-1, stack));
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAddWithSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(addInternal(slot, stack));
    }

    @Unique
    private boolean addInternal(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;

        Inventory inv = (Inventory)(Object)this;
        // 破损物品只能放入空槽
        if (stack.isDamaged()) {
            if (slot == -1) slot = inv.getFreeSlot(); // 使用修改后的 getFreeSlot
            if (slot >= 0) {
                inv.setItem(slot, stack.copyAndClear());
                inv.setChanged();
                return true;
            }
            return inv.player.hasInfiniteMaterials();
        }

        // 非破损物品：先尝试堆叠到已有槽位
        int remaining = stack.getCount();
        if (slot == -1) {
            // 先找可堆叠的槽（包括额外）
            int target = inv.getSlotWithRemainingSpace(stack); // 已修改支持额外
            while (target != -1 && remaining > 0) {
                ItemStack targetStack = inv.getItem(target);
                int maxAdd = inv.getMaxStackSize(targetStack) - targetStack.getCount();
                int add = Math.min(remaining, maxAdd);
                targetStack.grow(add);
                remaining -= add;
                targetStack.setPopTime(5);
                inv.setChanged();
                target = inv.getSlotWithRemainingSpace(stack);
            }
            // 再放入空槽
            if (remaining > 0) {
                int free = inv.getFreeSlot(); // 已修改支持额外
                if (free != -1) {
                    inv.setItem(free, stack.copyWithCount(remaining));
                    remaining = 0;
                    inv.setChanged();
                }
            }
        } else {
            if (isExtraSlot(slot) && !extraSlotEnabled[getExtraIndex(slot)]) {
                // 禁用槽，不可放入
                return false;
            }
            // 指定槽位
            ItemStack target = inv.getItem(slot);
            if (target.isEmpty()) {
                inv.setItem(slot, stack.copyWithCount(remaining));
                remaining = 0;
                inv.setChanged();
            } else if (ItemStack.isSameItemSameComponents(target, stack) && target.isStackable()) {
                int maxAdd = inv.getMaxStackSize(target) - target.getCount();
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
        return inv.player.hasInfiniteMaterials();
    }
}
