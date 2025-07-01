package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.api.BackPackSlot;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackpackCondition;
import com.yyz.yyzsbackpack.api.BackpackExclusionZoneProvider;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(value = AbstractContainerScreen.class,priority = 1001)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen implements BackpackExclusionZoneProvider {
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

    @Override
    public void renderBackground(GuiGraphics context) {
        super.renderBackground(context);

        // 传递renderCondition参数
        BackpackManager.renderBackpackBackground(context, leftPos, topPos, imageWidth, imageHeight,
                playerInventory, shouldRenderBackpackExtension,
                (BackpackCondition) this.menu);
    }


    @ModifyConstant(method = "checkHotbarMouseClicked", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPosition(int original) {
        return 40 + 9 * 6 + 1;
    }

    @ModifyConstant(method = "checkHotbarKeyPressed", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {
        return 40 + 9 * 6 + 1;
    }



    @Inject(method = "render", at = @At("HEAD"))
    private void checkBackpackStateChange(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean currentState = BackpackManager.shouldRenderBackpackExtension(menu,playerInventory);
        if (currentState != previousBackpackState) {
            shouldRenderBackpackExtension = currentState;
            previousBackpackState = currentState;
            rebuildWidgets();
        }
    }



    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseClicked(AbstractContainerScreen<?> instance, double mouseX, double mouseY,
                                       int left, int top, int button) {
        return menu instanceof BackpackCondition ?
                BackpackManager.isClickOutsideExtendedBounds(
                        playerInventory,
                        hasClickedOutside(mouseX, mouseY, leftPos, topPos, button),
                        mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight,
                        shouldRenderBackpackExtension,
                        (BackpackCondition) this.menu // 传递renderCondition
                ) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean handleMouseReleased(AbstractContainerScreen<?> instance, double mouseX, double mouseY,
                                        int left, int top, int button) {
        return menu instanceof BackpackCondition ?
                BackpackManager.isClickOutsideExtendedBounds(
                        playerInventory,
                        hasClickedOutside(mouseX, mouseY, leftPos, topPos, button),
                        mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight,
                        shouldRenderBackpackExtension,
                        (BackpackCondition) this.menu // 传递renderCondition
                ) : hasClickedOutside(mouseX, mouseY, leftPos, topPos, button);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeFields(AbstractContainerMenu handler, Inventory inventory, Component component, CallbackInfo ci) {
        this.playerInventory = inventory;
        this.shouldRenderBackpackExtension = BackpackManager.shouldRenderBackpackExtension(handler, playerInventory);
        this.previousBackpackState = shouldRenderBackpackExtension;

    }

    @Inject(
            method = "render",
            at = @At(
                    value = "HEAD"
            )
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
        ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
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

}