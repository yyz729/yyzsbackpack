package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryMenu.class)
public class AbstractMountInventoryMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(int i, Inventory inventory, Container container, AbstractHorse abstractHorse, int j, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((HorseInventoryMenu)(Object)this, inventory);
    }
}
