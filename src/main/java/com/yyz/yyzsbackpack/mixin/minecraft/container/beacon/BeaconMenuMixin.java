package com.yyz.yyzsbackpack.mixin.minecraft.container.beacon;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconMenu.class)
public class BeaconMenuMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Container inventory, ContainerData beaconData, ContainerLevelAccess access, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((BeaconMenu)(Object)this, (Inventory) inventory);
    }
}
