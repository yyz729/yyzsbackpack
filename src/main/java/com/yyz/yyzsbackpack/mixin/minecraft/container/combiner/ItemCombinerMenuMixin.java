package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((ItemCombinerMenu)(Object)this, inventory);
    }
}
