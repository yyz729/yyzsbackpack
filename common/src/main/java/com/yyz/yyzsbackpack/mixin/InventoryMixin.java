package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Inventory.class)
public abstract class InventoryMixin {


	@Shadow
	@Final
	public Player player;


	@Shadow public abstract int getSlotWithRemainingSpace(ItemStack itemStack);

	@Shadow public abstract int getFreeSlot();

	@Shadow public abstract ItemStack getItem(int i);

	@Shadow public abstract boolean add(int i, ItemStack itemStack);

	@ModifyArg(method = "<init>", index = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
	private int modifyMainSize(int size) {
		return size + 9 * 6 + 1;
	}

	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 40))
	private static int modifyOffhandSlotConstant(int original) {
		return 95;
	}

	@ModifyConstant(method = "getSlotWithRemainingSpace", constant = @Constant(intValue = 40))
	private int modifyOffhandSlotConstant1(int constant) {
		return 95;
	}

	@Redirect(method = "getFreeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyLoopCount(NonNullList<ItemStack> instance) {
		return BackpackHelper.getBackpackSize(player);
	}

	@Redirect(method = "findSlotMatchingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyFindSlotMatchingItem(NonNullList<ItemStack> instance) {
		return BackpackHelper.getBackpackSize(player);
	}
	@Redirect(method = "findSlotMatchingCraftingIngredient", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyFindSlotMatchingUnusedItem(NonNullList<ItemStack> instance) {
		return BackpackHelper.getBackpackSize(player);
	}

	@Redirect(method = "getSlotWithRemainingSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyGetSlotWithRemainingSpace(NonNullList<ItemStack> instance) {
		return BackpackHelper.getBackpackSize(player);
	}

	@Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), cancellable = true)
	private void placeItemBackInInventory(ItemStack itemStack, boolean bl, CallbackInfo ci) {
		ci.cancel();

		while(true) {
			if (!itemStack.isEmpty()) {
				int i = this.getSlotWithRemainingSpace(itemStack);
				if (i == -1) {
					i = this.getFreeSlot();
				}

				if (i != -1 && i < BackpackHelper.getBackpackSize(player)) {
					int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
					if (i>=36 && (BackpackHelper.isItemBlacklisted(itemStack.getItem()) || (!itemStack.getItem().canFitInsideContainerItems() && Backpack.getConfig().restrict_container_items))) {
						this.player.drop(itemStack, false); // 直接掉落物品
						return;
					}
					if (this.add(i, itemStack.split(j)) && bl && this.player instanceof ServerPlayer) {
						((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, i, this.getItem(i)));
					}
					continue;
				}

				this.player.drop(itemStack, false);
			}

			return;
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void addSlot(CallbackInfo ci) {
		if(BackpackPlatform.getEquipped(player).has(BackpackPlatform.getBackpackItemsComponent())){
			BackpackStorage.restoreBackpackContents(player.getInventory(), BackpackPlatform.getEquipped(player));
		}
	}



}
