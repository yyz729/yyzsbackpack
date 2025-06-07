package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.YyzsBackpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    // 原有字段
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow @Final protected T handler;
    @Unique
    private static final Identifier BONUS_ROWS_TEXTURE = new Identifier(YyzsBackpack.MOD_ID, "textures/gui/bonus_rows.png");
    @Unique
    private boolean shouldRenderBonusRows = false;
    @Unique
    private boolean lastBonusState = false;
    @Unique
    private PlayerInventory inventory;

    // 新增字段：用于GUI缩放调整
    @Unique
    private Integer originalGuiScale;
    @Unique
    private boolean wasAutoScale;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    // 在初始化时调整GUI缩放
    @Inject(method = "render", at = @At("HEAD"))
    private void onScreenInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        Window window = client.getWindow();

        // 如果已经调整过，则跳过
        if (originalGuiScale != null) return;

        // 计算当前有效缩放
        int currentScale = getEffectiveGuiScale(options, window);

        // 计算界面实际尺寸（包括扩展区域）
        int extendedWidth = getExtendedWidth();
        int totalWidth = backgroundWidth + extendedWidth;
        int totalHeight = backgroundHeight;

        // 计算虚拟屏幕尺寸（缩放后的分辨率）
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();

        // 计算界面位置（居中）
        int left = (scaledWidth - backgroundWidth) / 2 - extendedWidth;
        int top = (scaledHeight - backgroundHeight) / 2;

        // 检查是否超出屏幕边界
        if (left < 0 || top < 0 || (left + totalWidth) > scaledWidth || (top + totalHeight) > scaledHeight) {
            // 保存原始设置
            originalGuiScale = options.getGuiScale().getValue();
            wasAutoScale = (originalGuiScale == 0);

            // 计算新缩放值（至少为1）
            int newScale = Math.max(currentScale - 1, 1);
            options.getGuiScale().setValue(newScale);

            // 应用新设置
            client.execute(() -> {
                if (client.getWindow() != null) {
                    client.onResolutionChanged();
                }
            });
        }
    }



    @Inject(method = "close", at = @At("RETURN"))
    private void checkStateChange(CallbackInfo ci) {
        restoreOriginalScale();
    }

    @Unique
    private void restoreOriginalScale() {
        if (originalGuiScale != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            GameOptions options = client.options;

            // 恢复原始值
            options.getGuiScale().setValue(wasAutoScale ? 0 : originalGuiScale);

            // 重置标记
            originalGuiScale = null;
            wasAutoScale = false;

            // 应用恢复的设置
            client.execute(() -> {
                if (client.getWindow() != null) {
                    client.onResolutionChanged();
                }
            });
        }
    }

    // 获取有效缩放值（处理自动模式）
    @Unique
    private int getEffectiveGuiScale(GameOptions options, Window window) {
        int settingValue = options.getGuiScale().getValue();
        if (settingValue == 0) { // 自动模式
            int width = window.getWidth();
            int height = window.getHeight();
            int maxScale = 1;
            while (maxScale < 6 &&
                    width / (maxScale + 1) >= 320 &&
                    height / (maxScale + 1) >= 240) {
                maxScale++;
            }
            return maxScale;
        }
        return settingValue;
    }

    // 计算扩展区域宽度
    @Unique
    private int getExtendedWidth() {
        if (inventory == null) return 0;

        ItemStack stack = inventory.getStack(94);
        if (stack.getItem() instanceof BackpackItem) {
            BackpackItem backpack = (BackpackItem) stack.getItem();
            int columns = backpack.getBackpackType().getColumns();
            return 14 + columns * 18;
        }
        return 0;
    }

    // 以下是原有功能的保留部分（背包扩展绘制等）...
    @Override
    public void renderBackground(DrawContext context) {
        super.renderBackground(context);
        if (this.shouldRenderBonusRows) {
            int columns = 0;
            // 获取背包的列数
            ItemStack stack = inventory.getStack(94);
            if (stack.getItem() instanceof BackpackItem) {
                BackpackItem backpackItem = (BackpackItem) stack.getItem();
                columns = backpackItem.getBackpackType().getColumns();
            }

            // 动态计算宽度：每列18像素
            int width = 14 + columns * 18;
            int i = this.x - 14 - columns * 18 - 1;
            int j = this.y + (backgroundHeight - 174) / 2;

            // 计算纹理U坐标（第四个参数）
            int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;

            // 绘制动态宽度的纹理
            if (width > 0) {
                context.drawTexture(BONUS_ROWS_TEXTURE, i, j, u, 0, width, 174,462,174);
            }
            if (!(handler instanceof PlayerScreenHandler)) {
                context.drawTexture(new Identifier(YyzsBackpack.MOD_ID, "textures/gui/inventory.png"), x + backgroundWidth+1  , y + backgroundHeight - 108 , 0, 0, 32, 108,32,108);

            }
        }
    }

    @ModifyConstant(method = "onMouseClick(I)V", constant = @Constant(intValue = 40))
    private int changeOffhandSlot(int og) {
        return 40+9*5+1;
    }

    @ModifyConstant(method = "handleHotbarKeyPressed", constant = @Constant(intValue = 40))
    private int changeOffhandSlot2(int og) {
        return 40+9*5+1;
    }

    @Unique
    private boolean shouldRenderBonusRows() {
        if (MinecraftClient.getInstance().currentScreen != null && this instanceof RecipeBookProvider && ((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget().isOpen()) {
            return false;
        }
        if(MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen){
            return false;
        }
        if (this.inventory != null) {
            ItemStack stack = inventory.getStack(94);
            return stack.getItem() instanceof BackpackItem;
        }
        return false;
    }

    // 在渲染前检查状态变化
    @Inject(method = "render", at = @At("HEAD"))
    private void checkStateChange(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean currentState = shouldRenderBonusRows();
        if (currentState != lastBonusState) {
            shouldRenderBonusRows = currentState;
            lastBonusState = currentState;
            clearAndInit(); // 状态变化时重新初始化界面
        }
    }

    @Unique
    private boolean checkStateChange(double mouseX, double mouseY, int left, int top, int button){
        // 原始范围检查
        boolean originalBl = mouseX < (double)left || mouseY < (double)top ||
                mouseX >= (double)(left + this.backgroundWidth) ||
                mouseY >= (double)(top + this.backgroundHeight);

        // 扩展范围检查（当鼠标在左侧时）
        boolean extendedArea = false;
        if (mouseX < (double)left && this.shouldRenderBonusRows) {
            int columns = 0;
            // 动态获取背包列数
            ItemStack stack = inventory.getStack(94);
            if (stack.getItem() instanceof BackpackItem) {
                BackpackItem backpackItem = (BackpackItem) stack.getItem();
                columns = backpackItem.getBackpackType().getColumns();
            }

            // 根据背包列数计算扩展区域
            extendedArea = mouseX >= (double)(left - 14 - 18 * columns) &&
                    mouseY >= (double)(top - 4) &&
                    mouseY < (double)(top + this.backgroundHeight + 4);
        }

        // 组合判断：原始范围 OR (扩展区域存在时覆盖扩展区域)
        boolean finalBl = originalBl && !extendedArea;

        if (MinecraftClient.getInstance().currentScreen != null && this instanceof RecipeBookProvider ) {

            return (((RecipeBookProvider) MinecraftClient.getInstance().currentScreen).getRecipeBookWidget().isClickOutsideBounds(mouseX, mouseY, this.x, this.y,
                    this.backgroundWidth, this.backgroundHeight, button) && finalBl);
        }else {
            return (finalBl);
        }
    }
    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isClickOutsideBounds(DDIII)Z"))
    private boolean injected(HandledScreen instance, double mouseX, double mouseY, int left, int top, int button) {
        return checkStateChange(mouseX,mouseY,left,top,button);
    }
    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isClickOutsideBounds(DDIII)Z"))
    private boolean injected1(HandledScreen instance, double mouseX, double mouseY, int left, int top, int button) {
        return checkStateChange(mouseX,mouseY,left,top,button);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void initFields(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.inventory = inventory;
        this.shouldRenderBonusRows = shouldRenderBonusRows();
        this.lastBonusState = shouldRenderBonusRows;
    }

}