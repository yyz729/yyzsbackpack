package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemEntity.class,priority = 999)
public class ItemEntityMixin {

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean modifyFindSlotMatchingItem(Inventory inventory, ItemStack stack) {
		int i = inventory.getSlotWithRemainingSpace(stack);
		if(inventory.getFreeSlot() >= 36 && inventory.getFreeSlot() < 36 + BackpackHelper.getMaxBackpackSize()) {
			return !BackpackHelper.isItemBlacklisted(stack.getItem())
					&& (!(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items))
					&& inventory.getFreeSlot() < BackpackHelper.getBackpackSize(inventory.player)
					&& inventory.add(stack);
		}else {
			return inventory.add(stack);
		}
	}


}

//package com.yyz.yyzsbackpack.mixin;
//
//import com.yyz.yyzsbackpack.Backpack;
//import com.yyz.yyzsbackpack.util.BackpackHelper;
//import net.minecraft.world.entity.item.ItemEntity;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.item.ItemStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(value = ItemEntity.class, priority = 999)
//public class ItemEntityMixin {
//
//	@Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
//	private boolean modifyFindSlotMatchingItem(Inventory inventory, ItemStack stack) {
//		// 1. 始终优先尝试主物品栏堆叠
//		int existingSlot = inventory.getSlotWithRemainingSpace(stack);
//		if (existingSlot != -1 && existingSlot < 36) {
//			return inventory.add(stack); // 主物品栏有可堆叠槽位，直接使用原版逻辑
//		}
//
//		// 2. 检查扩展背包条件
//		int firstFreeSlot = inventory.getFreeSlot();
//		boolean isBackpackSlot = (firstFreeSlot >= 36 && firstFreeSlot < 36 + BackpackHelper.getMaxBackpackSize());
//
//		if (isBackpackSlot) {
//			// 3. 检查物品是否允许放入背包
//			boolean isAllowed = !BackpackHelper.isItemBlacklisted(stack.getItem())
//					&& (!(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items))
//					&& firstFreeSlot < (36 + BackpackHelper.getBackpackSize(inventory.player));
//
//			// 4. 优先尝试主物品栏空槽
//			if (isAllowed && inventory.getFreeSlot() < 36) {
//				return inventory.add(stack); // 主物品栏有空槽，使用原版逻辑
//			}
//
//			// 5. 最后尝试扩展背包
//			return isAllowed && inventory.add(stack);
//		}
//
//		// 6. 默认情况使用原版逻辑
//		return inventory.add(stack);
//	}
//}
