package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackPackSlot;
import com.yyz.yyzsbackpack.api.BackpackCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen {
    // 基础GUI字段
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Shadow protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button);

    @Shadow @Final protected T menu;
    // 背包相关字段
    @Unique
    private Inventory playerInventory;
    @Unique
    private boolean shouldRenderBackpackExtension = false;
    @Unique
    private boolean previousBackpackState = false;

    protected HandledScreenMixin(Component title) {
        super(title);

    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void onRenderBackground(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BackpackManager.renderBackpackBackground(context, leftPos, topPos, imageWidth, imageHeight, playerInventory, shouldRenderBackpackExtension, (BackpackCondition) this.menu);
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
        boolean currentState = BackpackManager.shouldRenderBackpackExtension(menu,playerInventory);
        if (currentState != previousBackpackState) {
            shouldRenderBackpackExtension = currentState;
            previousBackpackState = currentState;
            rebuildWidgets();
        }
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseClicked(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackCondition ? BackpackManager.isClickOutsideExtendedBounds(playerInventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpackExtension, (BackpackCondition) this.menu ) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseReleased(AbstractContainerScreen<?> instance, double mouseX, double mouseY, int left, int top, int button) {
        return menu instanceof BackpackCondition ? BackpackManager.isClickOutsideExtendedBounds(playerInventory, hasClickedOutside(mouseX, mouseY, leftPos, topPos, button), mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, shouldRenderBackpackExtension, (BackpackCondition) this.menu) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component, CallbackInfo ci) {
        this.playerInventory = inventory;
        this.shouldRenderBackpackExtension = BackpackManager.shouldRenderBackpackExtension(menu, playerInventory);
        this.previousBackpackState = shouldRenderBackpackExtension;
    }


    @Inject(
            method = "renderContents",
            at = @At("HEAD")
    )
    private void updateBackpackSlotsPositionBeforeRender(
            GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci
    ) {

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
}