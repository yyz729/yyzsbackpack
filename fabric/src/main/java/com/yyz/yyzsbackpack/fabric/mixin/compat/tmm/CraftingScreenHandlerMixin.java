package com.yyz.yyzsbackpack.fabric.mixin.compat.tmm;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractContainerMenu {
    protected CraftingScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lsmartin/miapi/blocks/ModularWorkBenchEntity;Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/inventory/ContainerData;)V", constant = @Constant(intValue = 40))
    private int modifyOffhandSlotConstant(int constant) {
        return constant + BackpackHelper.getSlotIndexOffset();
    }
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lsmartin/miapi/blocks/ModularWorkBenchEntity;Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/inventory/ContainerData;)V", constant = @Constant(intValue = 39))
    private int modifyArmorSlotConstant(int constant) {
        return constant + BackpackHelper.getSlotIndexOffset();
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lsmartin/miapi/blocks/ModularWorkBenchEntity;Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void addSlots(int syncId, Inventory inventory, ModularWorkBenchEntity benchEntity, ContainerLevelAccess context, ContainerData delegate, CallbackInfo ci) {
        SlotManager.addBackpackInventorySlots(this,inventory);
        ((BackpackMenu)this).setBackpackVisible(true);
    }
}
