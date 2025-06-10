package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {


	@Shadow
	@Final
	public PlayerEntity player;

	@Shadow
	@Final
	public DefaultedList<ItemStack> main;


	@ModifyArg(method = "<init>", index = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/collection/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/collection/DefaultedList;"))
	private int modifyMainSize(int size) {
		return size + 9 * 6 + 1;
	}

	

	@Redirect(
			method = "getEmptySlot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/collection/DefaultedList;size()I"
			)
	)
	private int modifyLoopCount(DefaultedList<ItemStack> list) {


		// 检查护甲槽位4（背包槽位）是否有背包物品
		ItemStack backpackStack = main.get(36);
		if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
			// 基础槽位数 + 背包列数 * 9
			return 36 + backpackItem.getBackpackType().getColumns() * 9;
		}
		return 36; // 没有背包时返回基础槽位数
	}




}
