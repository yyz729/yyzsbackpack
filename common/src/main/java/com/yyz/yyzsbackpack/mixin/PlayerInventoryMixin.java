package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
	@Shadow
	@Final
	public Player player;

	@ModifyArg(method = "<init>", index = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
	private int modifyMainSize(int size) {
		return 91;
	}

	@Redirect(method = "getFreeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
	private int modifyLoopCount(NonNullList<ItemStack> instance) {
		// 检查是否有背包物品
		ItemStack backpackStack = BackpackHelper.getEquipped(player);
		if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
			// 基础槽位数 + 背包列数 * 9
			return 36 + backpackItem.getBackpackType().getColumns() * 9;
		}
		return 36; // 没有背包时返回基础槽位数
	}


	@Inject(method = "tick", at = @At("RETURN"))
	private void addSlot(CallbackInfo ci) {
		if(BackpackHelper.getEquipped(player).getOrCreateTag().contains("BackpackItems")){
			BackpackManager.restoreBackpackContents(player.getInventory(), BackpackHelper.getEquipped(player));
		}
	}
}
