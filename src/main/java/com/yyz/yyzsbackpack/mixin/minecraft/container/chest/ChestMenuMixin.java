package com.yyz.yyzsbackpack.mixin.minecraft.container.chest;

import com.yyz.yyzsbackpack.api.IBackpackMenu;
import com.yyz.yyzsbackpack.api.BackpackContainerHelper;
import com.yyz.yyzsbackpack.mixin.AbstractBackpackMenuMixin;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestMenu.class)
public class ChestMenuMixin{
    @Unique
    private int backpackSlotStart;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, int containerId, Inventory inventory, Container container, int rows, CallbackInfo ci) {
        BackpackContainerHelper.addBackpackSlotsIfPresent((ChestMenu)(Object)this, inventory);
    }

//    @Override
//    public int yyzsbackpack$getBackpackSlotStart() {
//        return backpackSlotStart;
//    }
//
//    @Override
//    public void yyzsbackpack$setBackpackSlotStart(int start) {
//        this.backpackSlotStart = start;
//    }
}
