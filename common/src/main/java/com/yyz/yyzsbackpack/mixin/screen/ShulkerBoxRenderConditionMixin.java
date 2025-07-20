package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.base.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreen.class)
public class ShulkerBoxRenderConditionMixin{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(ShulkerBoxMenu shulkerBoxMenu, Inventory inventory, Component component, CallbackInfo ci) {
        ((BackpackCondition)shulkerBoxMenu).setRenderBackpack(true);
    }
}
