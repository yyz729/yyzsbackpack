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

@Mixin(BrewingStandScreenHandler.class)
public abstract class BrewingStandScreenHandlerMixin extends ScreenHandler {
    protected BrewingStandScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V", at = @At("RETURN"))
    private void addMoreRows(int syncId, PlayerInventory inventory, Inventory inventory1, PropertyDelegate propertyDelegate, CallbackInfo ci) {
        YyzsBackpack.addBackpack(this,inventory, 176, 166, false);
    }
}