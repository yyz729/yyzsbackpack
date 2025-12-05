package com.yyz.yyzsbackpack.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.neoforge.BackpackNeoForge;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ItemColorsMixin {

//    @Inject(method = "createDefault", at = @At("RETURN"))
//    private static void injected(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir, @Local ItemColors itemColors) {
//        itemColors.register((itemStack, i) -> i > 0 ? -1 : DyedItemColor.getOrDefault(itemStack, -6265536), BackpackNeoForge.IRON_BACKPACK.get(),BackpackNeoForge.GOLD_BACKPACK.get(),BackpackNeoForge.DIAMOND_BACKPACK.get(),BackpackNeoForge.NETHERITE_BACKPACK.get());
//    }
}
