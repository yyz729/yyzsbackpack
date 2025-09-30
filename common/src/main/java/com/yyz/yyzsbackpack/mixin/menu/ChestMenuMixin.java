package com.yyz.yyzsbackpack.mixin.menu;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChestMenu.class)
public abstract class ChestMenuMixin extends AbstractContainerMenu {
    protected ChestMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @ModifyArg(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ChestMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z",ordinal = 0), index = 2)
    private int injected(int x) {
        return x - BackpackHelper.getMaxBackpackSize();
    }
}