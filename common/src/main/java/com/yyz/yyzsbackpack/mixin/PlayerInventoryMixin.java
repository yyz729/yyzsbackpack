package com.yyz.yyzsbackpack.mixin;

import com.google.common.collect.ImmutableList;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.util.List;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin implements Container {
	@Shadow
	@Final
	public Player player;

	@Shadow public abstract void setItem(int i, ItemStack arg);

	@Shadow public abstract @NotNull ItemStack getItem(int i);

	@Shadow @Final public NonNullList<ItemStack> items;

	@Shadow public abstract int getSlotWithRemainingSpace(ItemStack arg);

	@Shadow public abstract int getFreeSlot();

	@Shadow public abstract boolean add(int j, ItemStack arg);

	@Shadow @Final public NonNullList<ItemStack> armor;

	@Shadow @Final public NonNullList<ItemStack> offhand;

	@Shadow @Final private List<NonNullList<ItemStack>> compartments;

	@Shadow protected abstract int addResource(ItemStack itemStack);

	@Shadow protected abstract int addResource(int i, ItemStack itemStack);

	@ModifyArg(method = "<init>", index = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
	private int modifyMainSize(int size) {
		return 91;
	}

	@Redirect(method = "getFreeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyLoopCount(NonNullList<ItemStack> instance) {
		return BackpackManager.getBackpackSize(player);
	}
	@Redirect(method = "findSlotMatchingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyFindSlotMatchingItem(NonNullList<ItemStack> instance) {
		return BackpackManager.getBackpackSize(player);
	}
	@Redirect(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyFindSlotMatchingUnusedItem(NonNullList<ItemStack> instance) {
		return BackpackManager.getBackpackSize(player);
	}

	@Redirect(method = "getSlotWithRemainingSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyGetSlotWithRemainingSpace(NonNullList<ItemStack> instance) {
		return BackpackManager.getBackpackSize(player);
	}

	@Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), cancellable = true)
	private void placeItemBackInInventory(ItemStack itemStack, boolean bl, CallbackInfo ci) {
		ci.cancel();
		// 检查物品是否应被禁止放入背包
		if (!itemStack.isEmpty() && (BackpackManager.disableBackpack(itemStack.getItem()) || (!itemStack.getItem().canFitInsideContainerItems() && Backpack.getConfig().container_item))) {
			this.player.drop(itemStack, false); // 直接掉落物品
			return;
		}
		while(true) {
			if (!itemStack.isEmpty()) {
				int i = this.getSlotWithRemainingSpace(itemStack);
				if (i == -1) {
					i = this.getFreeSlot();
				}

				if (i != -1 && i < BackpackManager.getBackpackSize(player)) {
					int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
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
		if(BackpackHelper.getEquipped(player).getOrCreateTag().contains("BackpackItems")){
			BackpackManager.restoreBackpackContents(player.getInventory(), BackpackHelper.getEquipped(player));
		}
	}

	public boolean modAdd(int i, ItemStack itemStack) {
		if (itemStack.isEmpty() ) {
			return false;
		} else {
			try {
				if (itemStack.isDamaged()) {
					if (i == -1) {
						i = this.getFreeSlot();
					}

					if (i >= 0) {
						this.items.set(i, itemStack.copyAndClear());
						((ItemStack)this.items.get(i)).setPopTime(5);
						return true;
					} else if (this.player.getAbilities().instabuild) {
						itemStack.setCount(0);
						return true;
					} else {
						return false;
					}
				} else {
					int j;
					do {
						j = itemStack.getCount();
						if (i == -1) {
							itemStack.setCount(this.addResource(itemStack));
						} else {
							itemStack.setCount(this.addResource(i, itemStack));
						}
					} while(!itemStack.isEmpty() && itemStack.getCount() < j);

					if (itemStack.getCount() == j && this.player.getAbilities().instabuild) {
						itemStack.setCount(0);
						return true;
					} else {
						return itemStack.getCount() < j;
					}
				}
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.forThrowable(throwable, "Adding item to inventory");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
				crashReportCategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
				crashReportCategory.setDetail("Item data", itemStack.getDamageValue());
				crashReportCategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
				throw new ReportedException(crashReport);
			}
		}
	}
//	@Unique
//	private static final int BASE_SIZE = 36; // 原版物品栏大小
//	@Unique
//	private static final int EXTENDED_SIZE = 91; // 修改后的物品栏大小
//
//	@Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
//	private void handleExtendedDrops(CallbackInfo ci) {
//		boolean dropExtended = true;
//
//
//		for (int compartmentIdx = 0; compartmentIdx < compartments.size(); compartmentIdx++) {
//			NonNullList<ItemStack> list = compartments.get(compartmentIdx);
//			for (int slot = 0; slot < list.size(); slot++) {
//				ItemStack stack = list.get(slot);
//				if (!stack.isEmpty()) {
//					// 只处理主物品栏的扩展槽位
//					if (compartmentIdx == 0 && slot >= BASE_SIZE && slot < EXTENDED_SIZE) {
//						if (dropExtended) {
//							player.drop(stack, true, false);
//						}
//					} else {
//						player.drop(stack, true, false);
//					}
//					list.set(slot, ItemStack.EMPTY);
//				}
//			}
//		}
//		ci.cancel(); // 取消原方法执行
//	}
}
