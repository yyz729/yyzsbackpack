package com.yyz.yyzsbackpack.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackScreen;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.client.BackpackKeyBinding;
import com.yyz.yyzsbackpack.client.BackpackRenderer;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements BackpackScreen {
    // 基础GUI字段
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Shadow protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button);

    @Shadow @Final protected T menu;
    @Shadow @Nullable
    protected Slot hoveredSlot;

    @Shadow protected abstract void slotClicked(Slot arg, int i, int j, ClickType arg2);

    @Shadow protected boolean isQuickCrafting;
    // 背包相关字段
    @Unique
    private Inventory inventory;
    @Unique
    private boolean shouldRenderBackpack = false;
    @Unique
    private boolean previousBackpackState = false;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void renderBackpackBackgroundAfterVanilla(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BackpackRenderer.renderEquippedBackpackBackground(context, leftPos, topPos, imageWidth, imageHeight, inventory, shouldRenderBackpack, (BackpackMenu) this.menu);
        BackpackRenderer.renderBackpackPreview(context,minecraft,menu,hoveredSlot,leftPos, topPos, imageWidth, imageHeight);
    }
    @ModifyConstant(method = "checkHotbarMouseClicked", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPosition(int original) {
        return 40 + BackpackHelper.getSlotIndexOffset();
    }

    @ModifyConstant(method = "checkHotbarKeyPressed", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + BackpackHelper.getSlotIndexOffset();
    }



    @Inject(method = "render", at = @At("HEAD"))
    private void checkBackpackStateChange(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RenderSystem.enableBlend();
        boolean currentState = BackpackHelper.shouldRenderBackpack(menu, inventory);
        if (currentState != previousBackpackState) {
            shouldRenderBackpack = currentState;
            previousBackpackState = currentState;
            rebuildWidgets();
        }
        RenderSystem.disableBlend();
    }



    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseClicked(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackMenu ? BackpackHelper.isClickOutsideExtendedBounds(inventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpack, (BackpackMenu) this.menu) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseReleased(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackMenu ? BackpackHelper.isClickOutsideExtendedBounds(inventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpack, (BackpackMenu) this.menu) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component, CallbackInfo ci) {
        this.inventory = inventory;
        this.shouldRenderBackpack = BackpackHelper.shouldRenderBackpack(menu, this.inventory);
        this.previousBackpackState = shouldRenderBackpack;
    }

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void updateBackpackSlotsPositionBeforeRender(
            GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci
    ) {
        RenderSystem.enableBlend();
        SlotManager.updateBackpackSlotPositions((Screen)(Object)this,menu,inventory,imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    public List<Rect2i> getBackpackExclusionZones() { // 修改返回类型为 Rect2i
        // 获取偏移值
        int xOffset = ((BackpackMenu) menu).getBackpackGuiX();
        int yOffset = ((BackpackMenu) menu).getBackpackGuiY();

        if (!shouldRenderBackpack) {
            return Collections.emptyList();
        }

        // 渲染背包时的完整计算
        int columns = 0;
        ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);
        if (backpackStack.getItem() instanceof BackpackItem backpack) {
            columns = backpack.getBackpackType().getColumns();
        }

        // 基础尺寸
        int baseWidth = 14 + columns * 18;
        int height = 174;

        // 计算位置
        int x = leftPos - baseWidth - 1 + xOffset;
        int y = topPos + (imageHeight - height) / 2 + yOffset;

        // 计算实际宽度（考虑偏移）
        int actualWidth = baseWidth;
        if (xOffset != 0) {
            if (xOffset > 0) {
                actualWidth += xOffset;
            } else {
                actualWidth -= xOffset;
            }
            actualWidth = Math.max(actualWidth, baseWidth);
        }

        return Collections.singletonList(new Rect2i(x, y, actualWidth, height)); // 使用 Rect2i
    }

    @Unique
    private Slot lastShiftHoveredSlot; // 记录上一次触发快速移动的槽位

    @Unique
    private boolean shiftPressed; // 跟踪Shift键是否按下

    @Unique
    private int type;


    //整理
    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if(BackpackKeyBinding.KEY_SORT.key == InputConstants.UNKNOWN) return;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), BackpackKeyBinding.KEY_SORT.key.getValue()) && menu.getCarried().isEmpty()) {

            int modifier = 2;
            if(Screen.hasShiftDown()) modifier = 3;
            if(Screen.hasControlDown()) modifier = 4;
            if(Screen.hasAltDown()) modifier = 5;

            if (hoveredSlot != null) {
                System.out.println(modifier);
                slotClicked(hoveredSlot, hoveredSlot.index, modifier, ClickType.QUICK_MOVE);
            }
        }
    }

    // 检测Shift键按下
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onKeyPressed(double d, double e, int i, CallbackInfoReturnable<Boolean> cir) {
        if (Screen.hasShiftDown() && menu.getCarried().isEmpty()) {
            shiftPressed = true;
            type = i;
        }
    }

    // 检测Shift键释放
    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onKeyReleased(double d, double e, int i, CallbackInfoReturnable<Boolean> cir) {
        if (shiftPressed) {
            shiftPressed = false;
            lastShiftHoveredSlot = null; // 重置记录
        }
    }

    // 处理鼠标移动
    @Inject(method = "mouseDragged", at = @At("TAIL"))
    private void onMouseMoved(double d, double e, int i, double f, double g, CallbackInfoReturnable<Boolean> cir) {
        if (shiftPressed && !isQuickCrafting) {
            // 检查是否有悬停的槽位且未处理过
            if (hoveredSlot != null && hoveredSlot != lastShiftHoveredSlot && hoveredSlot.hasItem()) {
                // 触发快速移动
                slotClicked(hoveredSlot, hoveredSlot.index, type, ClickType.QUICK_MOVE);
                lastShiftHoveredSlot = hoveredSlot; // 更新记录
            }
        }
    }
}