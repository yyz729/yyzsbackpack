package com.yyz.yyzsbackpack.forge.mixin.compat.curios;

import com.yyz.yyzsbackpack.BackpackManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;

@Mixin(CuriosContainerV2.class)
public abstract class CuriosContainerV2Mixin extends AbstractContainerMenu{


    protected CuriosContainerV2Mixin(@Nullable MenuType<?> arg, int i) {
        super(arg, i);
    }

    @ModifyConstant(method = "setPage", constant = @Constant(intValue = 36),remap = false)
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "setPage", constant = @Constant(intValue = 40),remap = false)
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("RETURN"),remap = false)
    private void addSlots(int windowId, Inventory inventory, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory);
//        BackpackManager.addEquippackSlot(this,inventory);
    }

}