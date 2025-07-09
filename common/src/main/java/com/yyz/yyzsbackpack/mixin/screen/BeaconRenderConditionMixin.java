package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.api.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public abstract class BeaconRenderConditionMixin extends AbstractContainerScreen<BeaconMenu> {
    public BeaconRenderConditionMixin(BeaconMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(BeaconMenu beaconMenu, Inventory inventory, Component component, CallbackInfo ci) {
//        ((BackpackCondition)beaconMenu).setRenderBackpack(true);

    }
}
