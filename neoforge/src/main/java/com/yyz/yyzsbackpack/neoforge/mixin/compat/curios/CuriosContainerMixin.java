package com.yyz.yyzsbackpack.neoforge.mixin.compat.curios;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import top.theillusivec4.curios.common.inventory.container.CuriosMenu;


@Mixin(CuriosMenu.class)
public abstract class CuriosContainerMixin extends AbstractContainerMenu{

    protected CuriosContainerMixin(@Nullable MenuType<?> arg, int i) {
        super(arg, i);
    }

    @ModifyConstant(method = "setPage", constant = @Constant(intValue = 39),remap = false)
    private int armorIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

    @ModifyConstant(method = "setPage", constant = @Constant(intValue = 40),remap = false)
    private int offhandIndexChange(int og) {
        return og + BackpackHelper.getSlotIndexOffset();
    }

}