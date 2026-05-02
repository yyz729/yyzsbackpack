package com.yyz.yyzsbackpack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {


    @ModifyVariable(
        method = "handleInventoryMouseClick",
        at = @At("HEAD"),
        argsOnly = true,
        index = 3
    )
    private int addBackpackOffset(int k, @Local(argsOnly = true) ClickType clickType) {

        int offset = BackpackHelper.getSlotIndexOffset();
        return clickType == ClickType.SWAP ? k + offset : k;
    }
}