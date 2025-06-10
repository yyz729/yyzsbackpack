package com.yyz.yyzsbackpack.mixin.screenhandler;

import com.yyz.yyzsbackpack.BackpackManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin extends ScreenHandler {
    protected ForgingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void addSlots(ScreenHandlerType type, int syncId, PlayerInventory inventory, ScreenHandlerContext context, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory, 176, 166);
    }
}