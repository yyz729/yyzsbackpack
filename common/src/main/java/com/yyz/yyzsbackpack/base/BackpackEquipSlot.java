package com.yyz.yyzsbackpack.base;

import com.mojang.datafixers.util.Pair;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackEquipSlot extends Slot {

    private final Container inventory;

    public BackpackEquipSlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
        this.inventory = container;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    @Override
    public void onTake(Player player, ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BackpackItem) {
            BackpackStorage.saveBackpackContents(inventory, backpackStack);
        }
        super.onTake(player, backpackStack);
    }

    @Override
    public void setByPlayer(ItemStack newBackpackStack) {
        ItemStack oldBackpackStack = this.getItem();
        if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
            BackpackStorage.saveBackpackContents(inventory, oldBackpackStack);
        }

        super.setByPlayer(newBackpackStack);

        if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
            BackpackStorage.restoreBackpackContents(inventory, newBackpackStack);
        }
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "item/backslot"));
    }
}
