package com.yyz.yyzsbackpack.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackPackSlot;
import com.yyz.yyzsbackpack.base.BackpackCondition;
import com.yyz.yyzsbackpack.base.BackpackExclusionZoneProvider;
import com.yyz.yyzsbackpack.base.EquipPackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen implements BackpackExclusionZoneProvider {
    // 基础GUI字段
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Shadow protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button);

    @Shadow @Final protected T menu;
    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow protected abstract void slotClicked(Slot slot, int i, int j, ClickType clickType);

    @Shadow protected boolean isQuickCrafting;
    // 背包相关字段
    @Unique
    private Inventory inventory;
    @Unique
    private boolean shouldRenderBackpackExtension = false;
    @Unique
    private boolean previousBackpackState = false;

    protected HandledScreenMixin(Component title) {
        super(title);

    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void onRenderBackground(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BackpackManager.renderBackpackBackground(context, leftPos, topPos, imageWidth, imageHeight, inventory, shouldRenderBackpackExtension, (BackpackCondition) this.menu);
    }

    @ModifyConstant(method = "checkHotbarMouseClicked", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPosition(int original) {
        return 40 + 9 * 6 + 1;
    }

    @ModifyConstant(method = "checkHotbarKeyPressed", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + 9 * 6 + 1;
    }

    @Inject(method = "renderContents", at = @At("HEAD"))
    private void checkBackpackStateChange(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean currentState = BackpackManager.shouldRenderBackpackExtension(menu, inventory);
        if (currentState != previousBackpackState) {
            shouldRenderBackpackExtension = currentState;
            previousBackpackState = currentState;
            rebuildWidgets();
        }
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseClicked(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackCondition ? BackpackManager.isClickOutsideExtendedBounds(inventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpackExtension, (BackpackCondition) this.menu ) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseReleased(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackCondition ? BackpackManager.isClickOutsideExtendedBounds(inventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpackExtension, (BackpackCondition) this.menu) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component, CallbackInfo ci) {
        this.inventory = inventory;
        this.shouldRenderBackpackExtension = BackpackManager.shouldRenderBackpackExtension(menu, this.inventory);
        this.previousBackpackState = shouldRenderBackpackExtension;
    }


    @Inject(method = "renderContents", at = @At("HEAD"))
    private void updateBackpackSlotsPositionBeforeRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen screen = (Screen)(Object)this;
        if (screen instanceof CreativeModeInventoryScreen) {
            return; // 跳过创造模式界面
        }

        if (menu instanceof BackpackCondition) {

            int baseHeight = imageHeight;
            // 查找第一个BackPackSlot类型的槽位索引
            int backpackSlotStartIndex = -1;
            for (int i = 0; i < menu.slots.size(); i++) {
                if (menu.slots.get(i) instanceof BackPackSlot) {
                    backpackSlotStartIndex = i;
                    break; // 找到第一个立即退出
                }
            }
            // 未找到背包槽位则直接返回
            if (backpackSlotStartIndex == -1) {
                return;
            }

            // 获取当前偏移值
            int xOffset = ((BackpackCondition) menu).getBackpackXOffset();
            int yOffset = ((BackpackCondition) menu).getBackpackYOffset();

            // 获取当前偏移值
            int xOffset1 = ((BackpackCondition) menu).getEquippackXOffset();
            int yOffset1 = ((BackpackCondition) menu).getEquippackYOffset();

            // 动态更新槽位位置
            BackpackManager.updateBackpackSlotsPosition(
                    menu, backpackSlotStartIndex,
                    baseHeight, xOffset, yOffset
            );
            // 动态更新装备槽位置
            BackpackManager.updateEquipmentSlotPosition(
                    menu,
                    baseHeight,
                    xOffset1,
                    yOffset1
            );
        }
    }
    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void renderBackpackContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {


        ((BackpackCondition)menu).setRenderTipBackpack(false);
        boolean requireKey;
        switch (Backpack.getConfig().tip_key.toLowerCase()) {
            case "shift" -> requireKey = hasShiftDown();
            case "alt" -> requireKey = hasAltDown();
            case "ctrl" -> requireKey = hasControlDown();
            case "none" -> requireKey = true; // 不需要按键
            default -> {
                // 无效配置时使用默认值（shift）
                if (!hasShiftDown()) return;
                return;
            }
        }

        // 如果配置要求按键但未按下，则返回
        if (!requireKey) return;

        if(this.hoveredSlot == null || !this.menu.getCarried().isEmpty()) return;

        ItemStack backpackStack = this.hoveredSlot.getItem();

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) return;
        BackpackManager.renderBackpackBackground1(guiGraphics,backpackStack, leftPos, topPos, imageWidth, imageHeight, (BackpackCondition) this.menu);
        ((BackpackCondition)menu).setRenderTipBackpack(true);
        // 从数据组件读取背包内容

        List<ItemStack> backpackItems = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());
        if (backpackItems == null) return;

        // 使用您的位置计算逻辑
        int baseHeight = this.imageHeight;
        int columns = backpackItem.getBackpackType().getColumns();
        int rows = 9; // 固定9行

        int startX = leftPos-25; // 基础X偏移
        int startY = topPos + (baseHeight - 166) / 2 + 3; // 基础Y位置


        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int slotIndex = column * rows + row;
                if (slotIndex >= backpackItems.size()) continue;

                // 计算每个物品的位置
                int x = startX - column * 18;
                int y = startY + row * 18;
                ItemStack stack = backpackItems.get(slotIndex);
                // 绘制物品图标
                guiGraphics.renderItem(stack, x, y);
                // 绘制物品数量
                guiGraphics.renderItemDecorations(minecraft.font, stack, x, y);
            }
        }
    }

    @Override
    public List<Rect2i> getBackpackExclusionZones() { // 修改返回类型为 Rect2i
        // 获取偏移值
        int xOffset = ((BackpackCondition) menu).getBackpackXOffset();
        int yOffset = ((BackpackCondition) menu).getBackpackYOffset();

        if (!shouldRenderBackpackExtension) {
            return Collections.emptyList();
        }

        // 渲染背包时的完整计算
        int columns = 0;
        ItemStack backpackStack = BackpackHelper.getEquipped(inventory.player);
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

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_R) && menu.getCarried().isEmpty()) {
            if (hoveredSlot != null) {
                slotClicked(hoveredSlot, hoveredSlot.index, 2, ClickType.QUICK_MOVE);
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