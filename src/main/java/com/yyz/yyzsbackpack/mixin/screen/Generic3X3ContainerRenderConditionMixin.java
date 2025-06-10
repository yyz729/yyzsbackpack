package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Generic3x3ContainerScreen.class)
public class Generic3X3ContainerRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(Generic3x3ContainerScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(true);
    }
}
