package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    // 基础GUI字段
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow @Final protected T handler;

    // 背包相关字段
    @Unique
    private PlayerInventory playerInventory;
    @Unique
    private boolean shouldRenderBackpackExtension = false;
    @Unique
    private boolean previousBackpackState = false;

    // GUI缩放管理字段
    @Unique
    private BackpackManager.Ref<Integer> originalGuiScaleRef = new BackpackManager.Ref<>(null);
    @Unique
    private BackpackManager.Ref<Boolean> wasAutoScaleRef = new BackpackManager.Ref<>(false);
    @Unique
    private int lastWindowWidth = -1;
    @Unique
    private int lastWindowHeight = -1;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        int currentWidth = window.getWidth();
        int currentHeight = window.getHeight();

        if (lastWindowWidth != currentWidth || lastWindowHeight != currentHeight) {
            lastWindowWidth = currentWidth;
            lastWindowHeight = currentHeight;
            BackpackManager.restoreOriginalScale(originalGuiScaleRef, wasAutoScaleRef);
            BackpackManager.adjustScaleIfNeeded(backgroundWidth, backgroundHeight, playerInventory,
                    originalGuiScaleRef, wasAutoScaleRef);
        } else if (originalGuiScaleRef.value == null) {
            BackpackManager.adjustScaleIfNeeded(backgroundWidth, backgroundHeight, playerInventory,
                    originalGuiScaleRef, wasAutoScaleRef);
        }
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void onScreenClose(CallbackInfo ci) {
        BackpackManager.restoreOriginalScale(originalGuiScaleRef, wasAutoScaleRef);
    }

    @Override
    public void renderBackground(DrawContext context) {
        super.renderBackground(context);
        BackpackManager.renderBackpackBackground(context, x, y, backgroundWidth, backgroundHeight,
                playerInventory, shouldRenderBackpackExtension,handler);
    }

    @ModifyConstant(method = "onMouseClick(I)V", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPosition(int original) {
        return 40 + 9 * 5 + 1;
    }

    @ModifyConstant(method = "handleHotbarKeyPressed", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + 9 * 5 + 1;
    }



    @Inject(method = "render", at = @At("HEAD"))
    private void checkBackpackStateChange(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean currentState = BackpackManager.shouldRenderBackpackExtension(playerInventory);
        if (currentState != previousBackpackState) {
            shouldRenderBackpackExtension = currentState;
            previousBackpackState = currentState;
            clearAndInit();
        }
    }



    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isClickOutsideBounds(DDIII)Z"))
    private boolean handleMouseClicked(HandledScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return BackpackManager.isClickOutsideExtendedBounds(playerInventory,mouseX, mouseY, left, top, button,backgroundWidth,backgroundHeight,shouldRenderBackpackExtension, x, y);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isClickOutsideBounds(DDIII)Z"))
    private boolean handleMouseReleased(HandledScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return BackpackManager.isClickOutsideExtendedBounds(playerInventory, mouseX, mouseY, left, top, button, backgroundWidth, backgroundHeight, shouldRenderBackpackExtension,x,y);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.playerInventory = inventory;
        this.shouldRenderBackpackExtension = BackpackManager.shouldRenderBackpackExtension(playerInventory);
        this.previousBackpackState = shouldRenderBackpackExtension;
    }
}