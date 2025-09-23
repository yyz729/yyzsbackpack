package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;

import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    List<MobEffect> yyzsbackpack$lastAppliedEffects = new ArrayList<>();

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        BackpackStorage.saveEquippedBackpackOnDeath(yyzsbackpack$player);
    }

    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onOpenMenu(CallbackInfo ci) {
        BackpackStorage.returnBackpackFromAccessorySlot(yyzsbackpack$player);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        BackpackStorage.restoreNonEmptyBackpack(yyzsbackpack$player);
        BackpackStorage.updateEffectsByBackpackCount(yyzsbackpack$player, yyzsbackpack$lastAppliedEffects);
    }
}