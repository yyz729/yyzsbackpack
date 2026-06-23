package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.api.*;
import com.yyz.yyzsbackpack.api.inventory.IBackpackMenu;
import com.yyz.yyzsbackpack.api.inventory.IExtendedInventory;
import com.yyz.yyzsbackpack.container.BackpackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractCraftingMenu implements IBackpackMenu {

    @Unique
    private int backpackSlotStart;

    public InventoryMenuMixin(MenuType<?> menuType, int containerId, int width, int height) {
        super(menuType, containerId, width, height);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {

        backpackSlotStart = this.slots.size();

        for (int i = 0; i < 256; i++) {
            this.addSlot(new BackpackSlot(inventory, backpackSlotStart + i, -1000, -1000, backpackSlotStart));
        }

    }

    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }
}
