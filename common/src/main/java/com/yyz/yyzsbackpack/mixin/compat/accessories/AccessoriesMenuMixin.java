package com.yyz.yyzsbackpack.mixin.compat.accessories;

import com.yyz.yyzsbackpack.api.BackpackCondition;
import io.wispforest.accessories.client.AccessoriesMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AccessoriesMenu.class)
public abstract class AccessoriesMenuMixin extends AbstractContainerMenu implements BackpackCondition {
    protected AccessoriesMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39))
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 40))
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

}
