package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.YyzsBackpack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreenHandler.class)
public abstract class ShulkerBoxScreenHandlerMixin extends ScreenHandler {
    protected ShulkerBoxScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }


    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At("RETURN"))
    private void addMoreRows(int syncId, PlayerInventory inventory, Inventory inventory1, CallbackInfo ci) {

        YyzsBackpack.addBackpack(this,inventory, 176, 168, false);
    }
}