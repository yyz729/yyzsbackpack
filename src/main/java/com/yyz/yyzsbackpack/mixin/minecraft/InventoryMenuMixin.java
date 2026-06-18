package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.BackpackSlot;
import com.yyz.yyzsbackpack.IExtendedInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractCraftingMenu {



    public InventoryMenuMixin(MenuType<?> menuType, int containerId, int width, int height) {
        super(menuType, containerId, width, height);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        for (int i = 0; i < IExtendedInventory.EXTRA_SLOT_COUNT; i++) {
            int slotIndex = IExtendedInventory.EXTRA_SLOT_START + i;
            int row = i / 9;
            int col = i % 9;
            int x = 8 + col * 18;
            int y = 160 + row * 18;
            this.addSlot(new BackpackSlot(inventory, slotIndex, x, y){});
        }

        IExtendedInventory ext = (IExtendedInventory) inventory;
        ext.yyzsbackpack$enableExtraSlots(9);
    }


}
