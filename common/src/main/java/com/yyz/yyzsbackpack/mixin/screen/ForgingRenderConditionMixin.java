package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCombinerScreen.class)
public class ForgingRenderConditionMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(ItemCombinerMenu itemCombinerMenu, Inventory inventory, Component component, ResourceLocation resourceLocation, CallbackInfo ci) {
        ((BackpackRenderCondition)itemCombinerMenu).setRenderBackpack(true);
    }
}
