package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IBackpackMenu {

    @Unique
    private int backpackSlotStart;

    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }

    @Override
    public void yyzsbackpack$setBackpackSlotStart(int start) {
        this.backpackSlotStart = start;
    }

    @Override
    public boolean yyzsbackpack$moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        return this.moveItemStackTo(stack, start, end, reverse);
    }

    @Shadow
    protected abstract boolean moveItemStackTo(ItemStack itemStack, int startSlot, int endSlot, boolean backwards);

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void onDoClick(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {

        // 只拦截 Shift + 右键（buttonNum == 1）的快速移动
        if (containerInput != ContainerInput.QUICK_MOVE || buttonNum != 1) {
            return; // 不拦截，继续原逻辑
        }
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;

        // 无效槽位检查
        if (slotIndex < 0 || slotIndex >= self.slots.size()) {
            return;
        }

        if(self.slots.get(slotIndex) instanceof BackpackSlot){
            return;
        }


        Slot slot = self.getSlot(slotIndex);
        if (!slot.mayPickup(player) || slot.getItem().isEmpty()) {
            return;
        }

        // 获取背包槽位起始索引
        int backpackStart = BackpackMenuHelper.getBackpackSlotStart(self);
        if (backpackStart == -1 || backpackStart >= self.slots.size()) {
            return; // 当前菜单没有添加背包槽位
        }

        // 记录原物品，用于后续更新
        ItemStack original = slot.getItem().copy();
        int originalCount = original.getCount();

        // 尝试将物品移入背包槽位（从 backpackStart 到 slots.size()）
        boolean moved = moveItemStackTo(original, backpackStart, self.slots.size(), false);


        if (moved) {
            // 更新原槽位：如果 original 被部分或全部移走，剩余部分留在原槽
            if (original.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.set(original);
            }
            // 触发 onTake（记录物品被取走，用于统计）
            int taken = originalCount - original.getCount();
            if (taken > 0) {
                slot.onTake(player, slot.getItem().copyWithCount(taken));
            }
            slot.setChanged();
        }

        // 不再执行原版
        ci.cancel();
    }

//    @Inject(method = "clicked", at = @At("HEAD"))
//    private void logClick(int slotIndex, int buttonNum, ContainerInput containerInput,
//                          Player player, CallbackInfo ci) {
//        // 获取当前菜单实例（混入对象）
//        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
//
//        // 构造槽位信息
//        String slotInfo;
//        if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
//            Slot slot = menu.slots.get(slotIndex);
//            ItemStack stack = slot.getItem();
//            slotInfo = String.format("Slot[%d] (%s) = %s",
//                    slotIndex,
//                    slot.getClass().getSimpleName(),
//                    stack.isEmpty() ? "empty" : stack.getDisplayName().getString() + " x" + stack.getCount());
//        } else {
//            slotInfo = "Slot index " + slotIndex + " (outside)";
//        }
//
//        // 打印日志
//        Backpack.LOGGER.info("Container Click - Player: {}, Slot: {}, Button: {}, Input: {}, Carried: {}",
//                player.getName().getString(),
//                slotInfo,
//                buttonNum,
//                containerInput,
//                menu.getCarried().isEmpty() ? "empty" :
//                        menu.getCarried().getDisplayName().getString() + " x" + menu.getCarried().getCount()
//        );
//    }
}