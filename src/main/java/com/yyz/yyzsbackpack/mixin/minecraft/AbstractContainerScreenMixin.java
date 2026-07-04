package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.BackpackContainerHelper;
import com.yyz.yyzsbackpack.api.BackpackScreenHelper;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDII)Z"
            )
    )
    private boolean redirectMouseClicked(
            AbstractContainerScreen<?> screen,
            double mouseX, double mouseY,
            int leftPos, int topPos
    ) {
        boolean original = hasClickedOutside(mouseX, mouseY, leftPos, topPos);
        if (original) {
            Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds(screen);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                return false;   // 点击在背包背景内
            }
        }
        return original;
    }

    @Redirect(
            method = "mouseReleased",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDII)Z"
            )
    )
    private boolean redirectMouseReleased(
            AbstractContainerScreen<?> screen,
            double mouseX, double mouseY,
            int leftPos, int topPos
    ) {
        boolean original = hasClickedOutside(mouseX, mouseY, leftPos, topPos);
        if (original) {
            Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds(screen);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return original;
    }
}