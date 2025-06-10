package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgingScreen.class)
public class ForgingRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(ForgingScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture, CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(true);
    }
}
