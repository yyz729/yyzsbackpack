package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots, CallbackInfo ci) {
        BackpackMenuHelper.addBackpackSlotsIfPresent((ItemCombinerMenu)(Object)this, inventory);
    }
}
