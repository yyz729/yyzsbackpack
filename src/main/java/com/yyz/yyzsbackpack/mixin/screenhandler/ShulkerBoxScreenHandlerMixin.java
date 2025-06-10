package com.yyz.yyzsbackpack.mixin.screenhandler;

import com.yyz.yyzsbackpack.BackpackManager;
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
    private void addSlots(int syncId, PlayerInventory inventory, Inventory inventory1, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory, 176, 166+1);
    }
}