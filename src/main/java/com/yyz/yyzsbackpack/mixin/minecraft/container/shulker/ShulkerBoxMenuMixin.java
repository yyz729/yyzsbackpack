package com.yyz.yyzsbackpack.mixin.minecraft.container.shulker;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxMenu.class)
public class ShulkerBoxMenuMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, Container ShulkerBox, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((ShulkerBoxMenu)(Object)this, inventory);
    }
}
