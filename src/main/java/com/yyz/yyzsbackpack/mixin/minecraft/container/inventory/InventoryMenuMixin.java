package com.yyz.yyzsbackpack.mixin.minecraft.container.inventory;

import com.yyz.yyzsbackpack.api.BackpackContainerHelper;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin implements IBackpackMenu {

    @Unique
    private int backpackSlotStart;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        BackpackContainerHelper.addBackpackSlotsIfPresent((AbstractContainerMenu)(Object)this, inventory);
    }


    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }

    @Override
    public void yyzsbackpack$setBackpackSlotStart(int start) {
        this.backpackSlotStart = start;
    }
}
