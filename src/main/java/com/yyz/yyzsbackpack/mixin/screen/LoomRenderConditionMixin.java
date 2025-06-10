package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomScreen.class)
public class LoomRenderConditionMixin  {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(LoomScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(true);
    }
}
