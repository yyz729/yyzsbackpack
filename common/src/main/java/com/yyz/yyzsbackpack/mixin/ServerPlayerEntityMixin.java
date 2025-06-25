package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        // 检查死亡不掉落规则是否启用
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return; // 如果启用则跳过后续处理
        }

        Inventory inventory = player.getInventory();
        ItemStack stack = BackpackHelper.getEquipped(player);

        if (stack.getItem() instanceof BackpackItem) {
            // 保存背包内容并清空槽位
            BackpackManager.saveBackpackContents(inventory, stack);
        }
    }
}