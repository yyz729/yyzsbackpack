package com.yyz.yyzsbackpack.mixin.screenhandler;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackpackCondition;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestMenu.class)
public abstract class GenericContainerScreenHandlerMixin extends AbstractContainerMenu {
    @Unique
    ChestMenu handler = (ChestMenu)(Object) this;

    protected GenericContainerScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }


    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At("RETURN"))
    private void addSlots(MenuType<?> menuType, int i, Inventory inventory, Container container, int j, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory, ((BackpackCondition) this).getInventory());//114+ handler.getRowCount() * 18

    }

    @ModifyArg(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ChestMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",ordinal = 0), index = 3)
    private boolean injected(boolean par4) {
        return false;
    }

}