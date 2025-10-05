package com.yyz.yyzsbackpack.mixin.menu;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserMenu.class)
public abstract class Generic3x3ContainerScreenHandlerMixin extends AbstractContainerMenu {


    protected Generic3x3ContainerScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("RETURN"))
    private void addSlots(int i, Inventory inventory, Container container, CallbackInfo ci) {

        SlotManager.addBackpackInventorySlots(this,inventory);
        ((BackpackMenu)this).setBackpackVisible(true);
    }
}