package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.NautilusInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NautilusInventoryMenu.class)
public class NautilusInventoryMenuMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, Container nautilusInventory, AbstractNautilus nautilus, int inventoryColumns, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((NautilusInventoryMenu)(Object)this, inventory);
    }
}
