package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperScreen.class)
public class HopperRenderConditionMixin{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(HopperScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(true);
    }
}
