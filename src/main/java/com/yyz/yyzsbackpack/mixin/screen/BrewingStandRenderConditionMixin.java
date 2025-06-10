package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandScreen.class)
public class BrewingStandRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(true);
    }
}
