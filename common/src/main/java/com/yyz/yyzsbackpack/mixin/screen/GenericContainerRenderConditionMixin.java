package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.base.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public class GenericContainerRenderConditionMixin{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(ChestMenu chestMenu, Inventory inventory, Component component, CallbackInfo ci) {
        ((BackpackCondition)chestMenu).setRenderBackpack(true);
    }
}
