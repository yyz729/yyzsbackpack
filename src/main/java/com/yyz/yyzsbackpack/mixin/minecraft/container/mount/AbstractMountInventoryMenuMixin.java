package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMountInventoryMenu.class)
public class AbstractMountInventoryMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, Container mountInventory, LivingEntity mount, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((AbstractMountInventoryMenu)(Object)this, inventory);
    }
}
