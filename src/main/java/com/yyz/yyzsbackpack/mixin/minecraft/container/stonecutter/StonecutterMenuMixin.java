package com.yyz.yyzsbackpack.mixin.minecraft.container.stonecutter;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterMenu.class)
public class StonecutterMenuMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((StonecutterMenu)(Object)this, inventory);
    }
}
