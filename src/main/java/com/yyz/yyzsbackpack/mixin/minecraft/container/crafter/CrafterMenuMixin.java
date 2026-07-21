package com.yyz.yyzsbackpack.mixin.minecraft.container.crafter;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterMenu.class)
public class CrafterMenuMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, CraftingContainer container, ContainerData containerData, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((CrafterMenu)(Object)this, inventory);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("RETURN"))
    private void onConstructSimple(int containerId, Inventory inventory, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((CrafterMenu)(Object)this, inventory);
    }
}
