package com.yyz.yyzsbackpack.fabric.mixin.compat.collective;

import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackManager;
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
	private boolean modifyFindSlotMatchingItem(Inventory instance, ItemStack stack) {
		Player player = instance.player;
		((CollectivePlayerEvents.Player_Picked_Up_Item)CollectivePlayerEvents.ON_ITEM_PICKED_UP.invoker()).onItemPickedUp(player.level(), player, stack);

		if(instance.getFreeSlot() >= 36 && instance.getFreeSlot() < 36+54) {
			return !BackpackManager.disableBackpack(stack.getItem())
					&& (!(!stack.getItem().canFitInsideContainerItems() && Backpack.getConfig().container_item))
					&& instance.getFreeSlot() < BackpackManager.getBackpackSize(instance.player)
					&& instance.add(stack);
		}else {
			return instance.add(stack);
		}
	}


}
