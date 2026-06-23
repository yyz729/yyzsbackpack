package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.api.inventory.IExtendedInventory;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin implements IExtendedInventory {

    @Unique
    private static final int EXTRA_SLOT_COUNT = 256;
    @Unique
    private static final int EXTRA_SLOT_START = 46;

    @Unique
    private final NonNullList<ItemStack> extraItems = NonNullList.withSize(EXTRA_SLOT_COUNT, ItemStack.EMPTY);
    @Unique
    private final boolean[] extraSlotEnabled = new boolean[EXTRA_SLOT_COUNT];

    @Unique
    private boolean backpackLoading = false;

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
        if (backpackLoading) return;
        backpackLoading = true;

        // 清空并禁用所有额外槽位
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            extraSlotEnabled[i] = false;
            extraItems.set(i, ItemStack.EMPTY);
        }

        if (backpack.getItem() instanceof BackpackItem backpackItem) {
            BackpackData data = backpackItem.getData();
            if (data != null) {
                int size = Math.min(data.size(), EXTRA_SLOT_COUNT);
                yyzsbackpack$enableExtraSlots(size); // 启用 size 个槽位

                // 从物品组件读取内容并复制到额外槽位
                ItemContainerContents contents = backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                NonNullList<ItemStack> temp = NonNullList.withSize(size, ItemStack.EMPTY);
                contents.copyInto(temp);
                for (int i = 0; i < size; i++) {
                    extraItems.set(i, temp.get(i));
                }
            }
        }

        backpackLoading = false;
    }

    @Override
    public void yyzsbackpack$syncToBackpack() {
        if (backpackLoading) return;
        Inventory inv = (Inventory)(Object)this;
        Player player = inv.player;
        if (player == null || player.level().isClientSide()) return; // 只服务端执行

        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestItem.getItem() instanceof BackpackItem)) return;

        NonNullList<ItemStack> contentsList = NonNullList.withSize(EXTRA_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < EXTRA_SLOT_COUNT; i++) {
            if (extraSlotEnabled[i]) {
                contentsList.set(i, extraItems.get(i).copy());
            }
        }
        ItemContainerContents newContents = ItemContainerContents.fromItems(contentsList);
        chestItem.set(DataComponents.CONTAINER, newContents);
    }

    @Unique
    private int getExtraIndex(int slot) { return slot - EXTRA_SLOT_START; }
    @Unique
    private boolean isExtraSlot(int slot) { return slot >= EXTRA_SLOT_START && slot < EXTRA_SLOT_START + EXTRA_SLOT_COUNT; }

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
            if (!backpackLoading) yyzsbackpack$syncToBackpack();
            ci.cancel();
            return;
        }
        if (slot == 38) {
            Inventory inv = (Inventory)(Object)this;
            if (!inv.player.level().isClientSide()) {
                ItemStack oldStack = inv.getItem(slot);
                if (oldStack.getItem() instanceof BackpackItem) {
                    yyzsbackpack$syncToBackpack(); // 将当前额外数据保存到旧物品
                }
            }
        }
    }

    // 加载新背包
    @Inject(method = "setItem", at = @At("RETURN"))
    private void onSetItemReturn(int slot, ItemStack stack, CallbackInfo ci) {
        if (slot == 38) {
            yyzsbackpack$syncFromBackpack(stack);
        }
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onRemoveItem(int slot, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            cir.setReturnValue(ContainerHelper.removeItem(extraItems, getExtraIndex(slot), count));
            if (!backpackLoading) yyzsbackpack$syncToBackpack();
        }
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true)
    private void onRemoveItemNoUpdate(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (isExtraSlot(slot)) {
            ItemStack stack = extraItems.get(getExtraIndex(slot));
            extraItems.set(getExtraIndex(slot), ItemStack.EMPTY);
            cir.setReturnValue(stack);
            if (!backpackLoading) yyzsbackpack$syncToBackpack();
        }
    }

    @Inject(method = "clearContent", at = @At("HEAD"))
    private void onClearContent(CallbackInfo ci) {
        extraItems.clear();
        if (!backpackLoading) yyzsbackpack$syncToBackpack();
    }

    // 填充合成辅助内容
    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void onFillStackedContents(StackedItemContents contents, CallbackInfo ci) {
        for (ItemStack stack : extraItems) {
            if (!stack.isEmpty()) {
                contents.accountSimpleStack(stack);
            }
        }
    }
}