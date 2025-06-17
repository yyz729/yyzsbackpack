package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreen.class)
public class GrindstoneRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(GrindstoneMenu grindstoneMenu, Inventory inventory, Component component, CallbackInfo ci) {
        ((BackpackRenderCondition)grindstoneMenu).setRenderBackpack(true);
    }
}
