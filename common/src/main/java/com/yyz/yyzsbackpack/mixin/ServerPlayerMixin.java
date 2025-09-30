package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
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
    }
    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onOpenMenu(CallbackInfo ci) {
        BackpackStorage.returnBackpackFromAccessorySlot(yyzsbackpack$player);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void addSlot(CallbackInfo ci) {
        BackpackStorage.restoreNonEmptyBackpack(yyzsbackpack$player);
        BackpackStorage.updateEffectsByBackpackCount(yyzsbackpack$player, yyzsbackpack$lastAppliedEffects);
    }

}