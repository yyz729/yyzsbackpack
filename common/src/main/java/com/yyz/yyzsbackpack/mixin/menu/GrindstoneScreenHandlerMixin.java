package com.yyz.yyzsbackpack.mixin.menu;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin extends AbstractContainerMenu {


    protected GrindstoneScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void addSlots(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {

        SlotManager.addBackpackInventorySlots(this,inventory);
        ((BackpackMenu)this).setBackpackVisible(true);
    }

}