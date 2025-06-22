package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryMenu.class,priority = 999)
public abstract class PlayerScreenHandlerMixin extends AbstractContainerMenu {


    protected PlayerScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, 0);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + 55;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + 55;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addSlots(Inventory inventory, boolean onServer, Player owner, CallbackInfo info) {

        BackpackManager.addBackpackSlots(this,inventory, ((BackpackRenderCondition) this).getInventory());
        BackpackManager.addEquippackSlot(this,inventory);
    }


}