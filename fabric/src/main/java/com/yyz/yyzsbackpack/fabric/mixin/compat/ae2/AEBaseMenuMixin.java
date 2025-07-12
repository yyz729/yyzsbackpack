package com.yyz.yyzsbackpack.fabric.mixin.compat.ae2;

import appeng.menu.AEBaseMenu;
import com.yyz.yyzsbackpack.BackpackManager;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AEBaseMenu.class)
public abstract class AEBaseMenuMixin extends AbstractContainerMenu {

    protected AEBaseMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Redirect(method = "createPlayerInventorySlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
    private int handleMouseReleased(NonNullList<Slot> instance) {
        return 36;
    }
    @Inject(method = "<init>", at = @At("RETURN"),remap = false)
    private void addSlots(MenuType<?> menuType, int id, Inventory inventory, Object host, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory);
    }

}
