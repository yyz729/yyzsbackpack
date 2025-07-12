package com.yyz.yyzsbackpack.forge.mixin.compat.wfha;

import moonfather.workshop_for_handsome_adventurer.block_entities.SimpleTableMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimpleTableMenu.class)
public abstract class SimpleTableMenuMixin extends AbstractContainerMenu {
    protected SimpleTableMenuMixin(@Nullable MenuType<?> arg, int i) {
        super(arg, i);
    }

//    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/inventory/MenuType;)V", at = @At("RETURN"),remap = false)
//    private void addSlots(int containerId, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType, CallbackInfo ci) {
//        BackpackManager.addBackpackSlots(this,inventory);
//    }
}
