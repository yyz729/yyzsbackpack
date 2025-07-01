package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.api.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DispenserMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserScreen.class)
public class Generic3X3ContainerRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(DispenserMenu dispenserMenu, Inventory inventory, Component component, CallbackInfo ci) {
        ((BackpackCondition)dispenserMenu).setRenderBackpack(true);
    }
}
