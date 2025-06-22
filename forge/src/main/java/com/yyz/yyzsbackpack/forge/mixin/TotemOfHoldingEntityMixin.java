package com.yyz.yyzsbackpack.forge.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.violetmoon.quark.addons.oddities.entity.TotemOfHoldingEntity;
import org.violetmoon.quark.content.tweaks.compat.TotemOfHoldingCuriosCompat;

import java.util.List;

@Mixin(TotemOfHoldingEntity.class)
public class TotemOfHoldingEntityMixin {
    @Shadow(remap = false) private List<ItemStack> equipedCurios;

    @Redirect(method = "skipAttackInteraction", at = @At(value = "INVOKE", target = "Lorg/violetmoon/quark/content/tweaks/compat/TotemOfHoldingCuriosCompat;equipCurios(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack injected(Player player, List<ItemStack> curiosItem, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
            BackpackManager.restoreBackpackContents(player.getInventory(), stack);
        }
        return stack = TotemOfHoldingCuriosCompat.equipCurios(player, this.equipedCurios, stack);
    }
}
