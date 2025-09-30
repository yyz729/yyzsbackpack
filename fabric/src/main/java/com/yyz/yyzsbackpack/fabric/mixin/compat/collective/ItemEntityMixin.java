package com.yyz.yyzsbackpack.fabric.mixin.compat.collective;

import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemEntity.class,priority = 1002)
public class ItemEntityMixin {

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean modifyFindSlotMatchingItem(Inventory inventory, ItemStack stack) {
		Player player = inventory.player;
		((CollectivePlayerEvents.Player_Picked_Up_Item)CollectivePlayerEvents.ON_ITEM_PICKED_UP.invoker()).onItemPickedUp(player.level(), player, stack);

		int i = inventory.getSlotWithRemainingSpace(stack);
		if(i >= 0 && i < BackpackHelper.getBackpackSize(inventory.player)){
			return inventory.add(i,stack);
		}
		if(inventory.getFreeSlot() >= 36 && inventory.getFreeSlot() < 36 + BackpackHelper.getMaxBackpackSize() ){
			return !BackpackHelper.isItemBlacklisted(stack.getItem())
					&& (!(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items))
					&& (inventory.getFreeSlot() < BackpackHelper.getBackpackSize(inventory.player) )
					&& inventory.add(stack);
		}else {
			return inventory.add(stack);
		}
	}


}
