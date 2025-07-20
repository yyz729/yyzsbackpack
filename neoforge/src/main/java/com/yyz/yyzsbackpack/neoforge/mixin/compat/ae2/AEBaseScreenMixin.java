package com.yyz.yyzsbackpack.neoforge.mixin.compat.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import com.yyz.yyzsbackpack.base.BackpackCondition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AEBaseScreen.class)
public abstract class AEBaseScreenMixin <T extends AEBaseMenu> extends AbstractContainerScreen<T> {
    public AEBaseScreenMixin(T arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void shouldRender(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        BackpackCondition condition = (BackpackCondition) menu;
        condition.setRenderBackpack(true);
        condition.setBackpackOffset(-20, 0);
    }
}
