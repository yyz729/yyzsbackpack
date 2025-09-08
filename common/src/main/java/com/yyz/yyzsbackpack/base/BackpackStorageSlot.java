package com.yyz.yyzsbackpack.base;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackStorageSlot extends Slot {
    private final AbstractContainerMenu menu;
//    private final int columnIndex;
    private final Inventory inventory;
    private final int slotIndex;
    public BackpackStorageSlot(AbstractContainerMenu menu, Inventory inventory, int index,  int j, int k) {
        super(inventory, index, j, k);
        this.menu = menu;
//        this.columnIndex = columnIndex;
        this.inventory = inventory;
        this.slotIndex = index;
    }

    @Override
    public boolean isActive() {
        ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);
        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return false;
        }

//        int columns = backpackItem.getBackpackType().getColumns();
//        if (columnIndex >= columns) {
//            return false;
//        }

        int size = backpackItem.getBackpackType().getSize();
        if(slotIndex >= size + 36){
            return false;
        }
        if(!(((BackpackMenu)menu).isBackpackVisible())){
            return false;
        }

        return true;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);
        boolean canPlace = !(stack.getItem() instanceof BackpackItem) &&
                backpackStack.getItem() instanceof BackpackItem &&
                super.mayPlace(stack);

        // 添加列数检查
        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
            int size = backpackItem.getBackpackType().getSize();
            if(slotIndex >= size + 36){
                return false;
            }

//            int columns = backpackItem.getBackpackType().getColumns();
//            if (columnIndex >= columns) {
//                return false;
//            }
        }
        if(BackpackHelper.isItemBlacklisted(stack.getItem())){
            return false;
        }

        if(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items){
            return false;
        }

        return canPlace;
    }
}
