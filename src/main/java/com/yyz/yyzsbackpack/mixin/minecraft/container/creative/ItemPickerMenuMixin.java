package com.yyz.yyzsbackpack.mixin.minecraft.container.creative;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public class ItemPickerMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Player player, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((AbstractContainerMenu) (Object) this, player.getInventory());
    }

    /**
     * 原版用 slots.size()-9 识别快捷栏。追加背包槽后会误把背包末尾当成快捷栏并清空。
     * 创造物品栏的快捷栏固定在 45~53（5×9 选择格之后）。
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void onQuickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (slotIndex >= 45 && slotIndex < 54 && slotIndex < menu.slots.size()) {
            Slot slot = menu.slots.get(slotIndex);
            if (slot != null && slot.hasItem()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}