package com.yyz.yyzsbackpack.mixin;

import com.mojang.datafixers.util.Pair;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends ScreenHandler {
    protected PlayerScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addSlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo info) {
        BackpackManager.addBackpackSlots(this,inventory, 8 + 69, 166);
        addSlot(new Slot(inventory, 36, 8 + 69 ,  8 + 18 * 2) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BackpackItem;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack backpackStack) {
                if (backpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(inventory, backpackStack);
                }
                super.onTakeItem(player, backpackStack);
            }

            @Override
            public void setStack(ItemStack newBackpackStack) {
                ItemStack oldBackpackStack = this.getStack();
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(inventory, oldBackpackStack);
                }

                super.setStack(newBackpackStack);

                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.restoreBackpackContents(inventory, newBackpackStack);
                }
            }


            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, BackpackManager.BACKSLOT_TEXTURE);
            }

        });
    }


}