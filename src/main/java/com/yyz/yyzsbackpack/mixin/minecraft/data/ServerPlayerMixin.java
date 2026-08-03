package com.yyz.yyzsbackpack.mixin.minecraft.data;

import com.mojang.authlib.GameProfile;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player{

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }


}
