package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean modifyFindSlotMatchingItem(Inventory instance, ItemStack stack) {
		if(instance.getFreeSlot() >= 36 && instance.getFreeSlot() < 36+54) {
			return !BackpackHelper.isItemBlacklisted(stack.getItem())
					&& (!(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items))
					&& instance.getFreeSlot() < BackpackHelper.getBackpackSize(instance.player)
					&& instance.add(stack);
		}else {
			return instance.add(stack);
		}
	}


}
