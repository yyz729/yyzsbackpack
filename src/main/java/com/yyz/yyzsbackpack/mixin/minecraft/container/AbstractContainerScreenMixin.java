package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.api.BackpackVisibilityHandler;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import com.yyz.yyzsbackpack.client.gui.BackpackToggleButton;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenInvoker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements BackpackVisibilityHandler {

    @Shadow
    protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);


    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    @Final
    protected int imageHeight;
    @Unique
    private boolean backpackVisible = true; // 默认可见

    @Override
    public boolean yyzsbackpack$isBackpackVisible() {
        return backpackVisible;
    }

    @Override
    public void yyzsbackpack$setBackpackVisible(boolean visible) {
        this.backpackVisible = visible;
        // 状态改变后需要立即刷新界面
        BackpackScreenHelper.setupBackpackSlots((AbstractContainerScreen<?>)(Object) this);
        BackpackScreenHelper.rebuildBackpackTabs((AbstractContainerScreen<?>)(Object) this);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int x = this.leftPos+3;
        int y = this.topPos +3;
        ((ScreenInvoker) this).invokeAddRenderableWidget(new BackpackToggleButton(x, y, this));
    }

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