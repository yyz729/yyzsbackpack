package com.yyz.yyzsbackpack.mixin.menu;

import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconMenu.class)
public abstract class BeaconScreenHandlerMixin extends AbstractContainerMenu {

    protected BeaconScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

//    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
//    private void addSlots(int i, Container container, ContainerData containerData, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {
//        BackpackManager.addBackpackSlots(this,container);
//    }

}