package com.yyz.yyzsbackpack.base;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EquipPackSlot extends Slot {

    private final Inventory inventory;
    private final AbstractContainerMenu menu;

    public EquipPackSlot(AbstractContainerMenu screenHandler, Inventory container, int i, int j, int k) {
        super(container, i, j, k);
        this.inventory = container;
        this.menu = screenHandler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }



    @Override
    public void onTake(Player player, ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.saveBackpackContents(inventory, backpackStack, true);
        }
        super.onTake(player, backpackStack);
    }

    @Override
    public void setByPlayer(ItemStack newBackpackStack) {
        ItemStack oldBackpackStack = this.getItem();
        if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.saveBackpackContents(inventory, oldBackpackStack, true);
        }

        super.setByPlayer(newBackpackStack);

        if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
            BackpackManager.restoreBackpackContents(inventory, newBackpackStack);
        }
    }

    @Override
    public ResourceLocation getNoItemIcon() {
        return BackpackManager.BACKSLOT_TEXTURE;
    }
}
