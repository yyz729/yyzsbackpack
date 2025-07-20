package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        Inventory inventory = player.getInventory();
        ItemStack stack = BackpackHelper.getEquipped(player);

        if (stack.getItem() instanceof BackpackItem) {
            // 保存背包内容并清空槽位
            BackpackManager.saveBackpackContents(inventory, stack,BackpackHelper.getEmptyRule(player));
        }
    }

    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onOpenMenu(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (player.level().isClientSide) return;

        Inventory inventory = player.getInventory();
        ItemStack stackInSlot36 = inventory.getItem(36+54);

        // 检查36号槽位是否有背包
        if (stackInSlot36.getItem() instanceof BackpackItem && BackpackManager.isTrinketModLoaded() && !Backpack.getConfig().force_slot) {
            // 保存背包内容到NBT
            BackpackManager.saveBackpackContents(inventory, stackInSlot36, true);

            // 从槽位移除背包
            inventory.setItem(36+54, ItemStack.EMPTY);

            // 将背包放回玩家背包
            if (!inventory.add(stackInSlot36)) {
                // 如果背包满了，则掉落物品
                player.spawnAtLocation(stackInSlot36, 1.0F);
            }
        }
    }
}