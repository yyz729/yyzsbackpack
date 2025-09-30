package com.yyz.yyzsbackpack.fabric.mixin.compat.reinforced;

import atonkish.reinfcore.client.gui.screen.ingame.ReinforcedStorageScreen;
import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReinforcedStorageScreen.class)
public abstract class ReinforcedStorageScreenMixin extends AbstractContainerScreen<ReinforcedStorageScreenHandler> {

    public ReinforcedStorageScreenMixin(ReinforcedStorageScreenHandler abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        ((BackpackMenu)menu).setBackpackVisible(true);
    }
}
