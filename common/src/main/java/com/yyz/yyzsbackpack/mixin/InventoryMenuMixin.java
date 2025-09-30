package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryMenu.class,priority = 999)
public abstract class InventoryMenuMixin extends AbstractCraftingMenu {


    public InventoryMenuMixin(MenuType<?> menuType, int i, int j, int k) {
        super(menuType, i, j, k);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addSlots(Inventory inventory, boolean onServer, Player owner, CallbackInfo info) {
        SlotManager.addBackpackEquipSlot(this, inventory);
    }

}