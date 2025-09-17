package com.yyz.yyzsbackpack.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.fabric.BackpackFabric;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ItemColorsMixin {

    @Inject(method = "createDefault", at = @At("RETURN"))
    private static void injected(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir, @Local ItemColors itemColors) {
        itemColors.register((itemStack, i) -> i > 0 ? -1 : DyedItemColor.getOrDefault(itemStack, -6265536), BackpackFabric.IRON_BACKPACK,BackpackFabric.GOLD_BACKPACK,BackpackFabric.DIAMOND_BACKPACK,BackpackFabric.NETHERITE_BACKPACK);
    }
}
