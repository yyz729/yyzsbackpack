package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackBackup;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Unique
    ServerPlayer yyzsbackpack$player = (ServerPlayer) (Object) this;

    @Unique
    List<Holder<MobEffect>> yyzsbackpack$lastAppliedEffects = new ArrayList<>();

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(CallbackInfo ci) {
        BackpackStorage.saveEquippedBackpackOnDeath(yyzsbackpack$player);
        if(!BackpackPlatform.getEmptyRule(yyzsbackpack$player)) return;
        for (int i = 0; i < BackpackHelper.getMaxBackpackSize(); i++) {
            yyzsbackpack$player.getInventory().setItem(36 + i, ItemStack.EMPTY);
        }
    }
    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onOpenMenu(CallbackInfo ci) {
        BackpackStorage.returnBackpackFromAccessorySlot(yyzsbackpack$player);
    }



    @Unique
    private int yyzsbackpack$backupTimer = 0;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        BackpackStorage.restoreNonEmptyBackpack(yyzsbackpack$player);
        BackpackStorage.updateEffectsByBackpackCount(yyzsbackpack$player, yyzsbackpack$lastAppliedEffects);

        int backupIntervalSeconds = Backpack.getConfig().backup_interval_seconds;
        int maxBackups = Backpack.getConfig().max_backup_count;

        // 如果间隔 <=0 或最大备份数 <=0，则禁用备份
        if (backupIntervalSeconds <= 0 || maxBackups <= 0) return;

        int intervalTicks = backupIntervalSeconds * 20;
        yyzsbackpack$backupTimer++;
        if (yyzsbackpack$backupTimer >= intervalTicks) {
            yyzsbackpack$backupTimer = 0;

            // 获取当前装备的背包物品
            ItemStack backpack = BackpackPlatform.getEquipped(yyzsbackpack$player);
            if (backpack.getItem() instanceof BackpackItem) {  // 需要 import BackpackItem
                // 获取正确的容器（兼容饰品栏）
//                Container container = BackpackPlatform.getContainer(yyzsbackpack$player);
                BackpackBackup.backupBackpackContents(backpack, yyzsbackpack$player.getInventory(), maxBackups);
            }
        }
    }
}