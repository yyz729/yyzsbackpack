package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackScroll;
import com.yyz.yyzsbackpack.api.IBackpackToggle;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements IBackpackToggle , IBackpackScroll {

    @Shadow
    protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);

    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Unique
    private static boolean backpackVisible = true; // 默认可见

    @Override
    public boolean yyzsbackpack$isBackpackVisible() {
        return backpackVisible;
    }

    @Override
    public void yyzsbackpack$setBackpackVisible(boolean visible) {
        backpackVisible = visible;
    }

    @Unique private int backpackScrollOffset = 0;
    @Unique private int backpackMaxScrollOffset = 0;

    @Override public int getScrollOffset() { return backpackScrollOffset; }
    @Override public void setScrollOffset(int offset) {
        backpackScrollOffset = Math.max(0, Math.min(offset, backpackMaxScrollOffset));
    }
    @Override public int getMaxScrollOffset() { return backpackMaxScrollOffset; }
    @Override public void setMaxScrollOffset(int max) { backpackMaxScrollOffset = Math.max(0, max); }


    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        Backpack.LOGGER.info("鼠标滚轮事件: x={}, y={}, scrollX={}, scrollY={}", x, y, scrollX, scrollY);
        Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds((InventoryScreen)(Object)this);
        if (bounds == null) {
            Backpack.LOGGER.info("bounds为空，不处理滚动");
            return;
        }
        Backpack.LOGGER.info("bounds: {}", bounds);
        if (bounds.contains(x, y)) {
            Backpack.LOGGER.info("鼠标在背包区域内，处理滚动");
            int delta = (int)Math.signum(scrollY);
            int oldOffset = getScrollOffset();
            int newOffset = oldOffset - delta;
            setScrollOffset(newOffset);
            Backpack.LOGGER.info("滚动偏移从 {} 变为 {}", oldOffset, newOffset);
            BackpackScreenHelper.setupBackpackSlots((InventoryScreen)(Object)this);
            cir.setReturnValue(true);
        } else {
            Backpack.LOGGER.info("鼠标不在背包区域内，不处理");
        }
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