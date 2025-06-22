package com.yyz.yyzsbackpack.neoforge.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.common.inventory.CurioSlot;

@Mixin(CurioSlot.class)
public abstract class CurioSlotMixin extends SlotItemHandler {
    @Shadow(remap = false) @Final private Player player;

    public CurioSlotMixin(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public void onTake(@NotNull Player player, ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.saveBackpackContents(player.getInventory(), backpackStack);
        }
        super.onTake(player, backpackStack);
    }

    @Override
    public void set(@NotNull ItemStack newBackpackStack) {
        ItemStack oldBackpackStack = this.getItem();
        if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.saveBackpackContents(player.getInventory(), oldBackpackStack);
        }

        super.set(newBackpackStack);

        if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.restoreBackpackContents(player.getInventory(), newBackpackStack);
        }
    }
}
