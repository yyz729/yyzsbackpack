package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.base.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterScreen.class)
public class StonecutterRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(StonecutterMenu stonecutterMenu, Inventory inventory, Component component, CallbackInfo ci) {
        ((BackpackCondition)stonecutterMenu).setRenderBackpack(true);
    }
}
