package com.yyz.yyzsbackpack.forge.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
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
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

@Mixin(CuriosContainer.class)
public abstract class CuriosContainerMixin extends AbstractContainerMenu{

    protected CuriosContainerMixin(@Nullable MenuType<?> arg, int i) {
        super(arg, i);
    }

    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Z)V", constant = @Constant(intValue = 36),remap = false)
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Z)V", constant = @Constant(intValue = 40),remap = false)
    private int offhandIndexChange(int og) {
        return og + 9 * 6 + 1;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("RETURN"),remap = false)
    private void addSlots(int windowId, Inventory inventory, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory, ((BackpackRenderCondition) this).getInventory());
//        BackpackManager.addEquippackSlot(this,inventory);
    }
}