package com.yyz.yyzsbackpack.mixin.minecraft.container.brewing;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandMenu.class)
public class BrewingStandMenuMixin {

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((BrewingStandMenu)(Object)this, inventory);
    }
}
