package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.BackpackRenderCondition;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements BackpackRenderCondition {

    @Shadow public abstract ItemStack getCursorStack();

    @Shadow @Final public DefaultedList<Slot> slots;

    @Shadow public abstract void setCursorStack(ItemStack stack);


    @Unique
    private boolean shouldRenderBackpack = false;

    @Override
    public boolean shouldRenderBackpack() {
        return this.shouldRenderBackpack;
    }



    @Override
    public void setRenderBackpack(boolean shouldRenderBackpack) {
        this.shouldRenderBackpack = shouldRenderBackpack;
    }

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void handleBackpackSwap(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {

        if (slotIndex < 0 || actionType != SlotActionType.PICKUP || slots.get(slotIndex).getStack().getItem() instanceof BackpackItem) return;

        if(slotIndex <= slots.size() - 55) return;
        ItemStack back = player.getInventory().getStack(36).copy();
        ItemStack stack = getCursorStack().copy();
        if(!(back.getItem() instanceof BackpackItem) || !(stack.getItem() instanceof BackpackItem)) return;
        BackpackManager.saveBackpackContents(player.getInventory(), back);
        BackpackManager.restoreBackpackContents(player.getInventory(),stack);
        player.getInventory().setStack(36,stack);
        setCursorStack(back);
    }
}
