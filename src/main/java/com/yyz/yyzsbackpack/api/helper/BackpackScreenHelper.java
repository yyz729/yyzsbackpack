package com.yyz.yyzsbackpack.api.helper;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.api.IBackpackScroll;
import com.yyz.yyzsbackpack.api.IBackpackToggle;
import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.data.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.data.LayoutOrder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import com.yyz.yyzsbackpack.client.gui.BackpackScrollbar;
import com.yyz.yyzsbackpack.client.gui.BackpackTabWidget;
import com.yyz.yyzsbackpack.client.gui.BackpackToggleButton;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenInvoker;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
import com.yyz.yyzsbackpack.network.SwitchBackpackC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public final class BackpackScreenHelper {

    private BackpackScreenHelper() {
    }

    /**
     * 根据背包数据重新计算所有背包槽位的实际显示位置，并设置到对应的 Slot 中。
     * 如果当前屏幕实现了 BackpackVisibilityHandler 且背包不可见，则将所有槽位移出屏幕。
     *
     * @param screen 目标容器屏幕
     */
    public static void setupBackpackSlots(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        int start = BackpackMenuHelper.getBackpackSlotStart(menu);
        if (start < 0) return;

        // 可见性检查
        boolean visible = !(screen instanceof IBackpackToggle handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) {
            for (int i = start; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                ((SlotAccessor) slot).setX(-1000);
                ((SlotAccessor) slot).setY(-1000);
            }
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) return;

        if (!(screen instanceof IBackpackScroll scrollable)) return;
        int scrollOffset = scrollable.getScrollOffset();

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        int slotCount = menu.slots.size();
        int[] origX = new int[slotCount];
        int[] origY = new int[slotCount];
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        // 遍历所有段，计算每个槽位的原始坐标，同时记录全局 minY / maxY
        for (LayoutSegment segment : data.segments()) {
            int segStart = segment.startSlot();
            int count = segment.getSlotCount();
            int baseX = segment.getEffectiveStartX() + offsetX;
            int baseY = segment.getEffectiveStartY() + offsetY;
            int columns = segment.columns().orElse(0);
            LayoutOrder order = segment.order();

            if (order == LayoutOrder.CUSTOM) {
                List<BackpackSlotPos> customPositions = segment.customPositions()
                        .orElseThrow(() -> new IllegalStateException("Missing customPositions"));
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slotCount) break;
                    BackpackSlotPos pos = customPositions.get(j);
                    origX[slotIndex] = pos.x() + offsetX;
                    origY[slotIndex] = pos.y() + offsetY;
                    if (origY[slotIndex] < minY) minY = origY[slotIndex];
                    if (origY[slotIndex] > maxY) maxY = origY[slotIndex];
                }
            } else {
                // 网格布局
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slotCount) break;
                    int relX = j % columns;
                    int relY = j / columns;
                    int x = baseX + relX * 18;
                    int y = baseY + relY * 18;
                    origX[slotIndex] = x;
                    origY[slotIndex] = y;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // 如果没有槽位，结束
        if (minY == Integer.MAX_VALUE || maxY == Integer.MIN_VALUE) return;

        int visibleRows = 7; // 固定显示7行
        // 计算真实的最大行索引（假设槽位 Y 坐标严格按 18 像素步进）
        int maxRow = (maxY - minY) / 18;
        int maxScroll = Math.max(0, maxRow - visibleRows + 1);
        scrollable.setMaxScrollOffset(maxScroll);

        // 修正当前滚动偏移
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
            scrollable.setScrollOffset(scrollOffset);
        }
        int pixelOffset = scrollOffset * 18;

        // 可视区域：以最小 Y 为顶部，向下 visibleRows 行
        int visibleTop = minY;
        int visibleBottom = visibleTop + visibleRows * 18;

        // 重新设置所有背包槽位
        for (int i = start; i < slotCount; i++) {
            Slot slot = menu.slots.get(i);
            int originalX = origX[i];
            int originalY = origY[i];
            int newY = originalY - pixelOffset;

            if (newY >= visibleTop - 2 && newY + 16 <= visibleBottom + 2) {
                ((SlotAccessor) slot).setX(originalX);
                ((SlotAccessor) slot).setY(newY);
            } else {
                ((SlotAccessor) slot).setX(-1000);
                ((SlotAccessor) slot).setY(-1000);
            }
        }
    }

    /**
     * 在屏幕背景之上绘制自定义背包 GUI 纹理。
     * 如果当前屏幕实现了 BackpackVisibilityHandler 且背包不可见，则不绘制。
     *
     * @param screen      目标容器屏幕
     * @param graphics    GuiGraphicsExtractor 实例
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     * @param partialTick 部分帧时间
     */
    public static void addBackpackBackground(AbstractContainerScreen<?> screen,
                                             GuiGraphicsExtractor graphics,
                                             int mouseX, int mouseY, float partialTick) {
        // 可见性检查
        if (screen instanceof IBackpackToggle handler && !handler.yyzsbackpack$isBackpackVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null || data.guiTexture() == null) {
            return;
        }

        int texWidth, texHeight;
        try {
            Resource resource = minecraft.getResourceManager().getResource(data.guiTexture()).orElseThrow();
            try (NativeImage image = NativeImage.read(resource.open())) {
                texWidth = image.getWidth();
                texHeight = image.getHeight();
            }
        } catch (Exception e) {
            return;
        }

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        int x = ((ScreenAccessor<?>) screen).getLeftPos() + data.backgroundX() + offsetX;
        int y = ((ScreenAccessor<?>) screen).getTopPos() + data.backgroundY() + offsetY;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                data.guiTexture(),
                x, y,
                0.0F, 0.0F,
                texWidth, texHeight,
                texWidth, texHeight
        );
    }

    /**
     * 计算当前背包背景纹理在屏幕上的矩形区域。
     * 如果当前屏幕实现了 BackpackVisibilityHandler 且背包不可见，返回 null。
     *
     * @param screen 当前容器屏幕
     * @return 矩形对象，若无背包背景或不可见则返回 null
     */
    @Nullable
    public static Rectangle getBackpackBackgroundBounds(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackToggle handler && !handler.yyzsbackpack$isBackpackVisible()) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return null;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null || data.guiTexture() == null) return null;

        int texWidth, texHeight;
        try {
            Resource resource = minecraft.getResourceManager().getResource(data.guiTexture()).orElseThrow();
            try (NativeImage image = NativeImage.read(resource.open())) {
                texWidth = image.getWidth();
                texHeight = image.getHeight();
            }
        } catch (Exception e) {
            return null;
        }

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        int x = ((ScreenAccessor<?>) screen).getLeftPos() + data.backgroundX() + offsetX;
        int y = ((ScreenAccessor<?>) screen).getTopPos() + data.backgroundY() + offsetY;

        return new Rectangle(x, y, texWidth, texHeight);
    }

    /**
     * 重建背包标签页控件。会根据当前屏幕的可见性状态决定是否显示标签。
     * 当可见性为 false 时，移除所有标签并返回；当可见性为 true 时，根据数据变化决定是否重建。
     *
     * @param screen 目标容器屏幕
     */
    public static void addBackpackTabs(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 获取当前所有标签
        List<BackpackTabWidget> oldTabs = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .map(w -> (BackpackTabWidget) w)
                .toList();

        // 检查可见性
        boolean visible = screen instanceof IBackpackToggle handler
                ? handler.yyzsbackpack$isBackpackVisible()
                : true;

        List<ItemStack> stacks = BackpackSlotHelper.getAllBackpackStacks(mc.player);

        // 如果不可见，移除所有已存在的标签并结束
        if (!visible) {
            if (!oldTabs.isEmpty()) {
                for (BackpackTabWidget tab : oldTabs) {
                    ((ScreenInvoker) screen).invokeRemoveWidget(tab);
                }
            }
            return;
        }

        // 可见但无背包，移除所有标签
        if (stacks.isEmpty()) {
            if (!oldTabs.isEmpty()) {
                for (BackpackTabWidget tab : oldTabs) {
                    ((ScreenInvoker) screen).invokeRemoveWidget(tab);
                }
            }
            return;
        }

        int selected = BackpackSlotHelper.getSelectedIndex(mc.player);
        if (selected >= stacks.size()) selected = 0;

        int left = ((ScreenAccessor<?>) screen).getLeftPos();
        int top = ((ScreenAccessor<?>) screen).getTopPos();
        int tabHeight = 18;
        int baseY = top - tabHeight - 2 + getOffsetY(screen);
        int currentOffsetX = getOffsetX(screen);

        // 检查是否需要重建
        boolean same = true;
        if (oldTabs.size() != stacks.size()) {
            same = false;
        } else {
            for (int i = 0; i < oldTabs.size(); i++) {
                BackpackTabWidget oldTab = oldTabs.get(i);
                if (!ItemStack.matches(oldTab.getIcon(), stacks.get(i))) {
                    same = false;
                    break;
                }
                if (oldTab.isSelected() != (i == selected)) {
                    same = false;
                    break;
                }
                int expectedX = left - (i + 1) * 20 + currentOffsetX;
                int expectedY = baseY;
                if (oldTab.getX() != expectedX || oldTab.getY() != expectedY) {
                    same = false;
                    break;
                }
            }
        }

        if (same) {
            return; // 无变化，无需重建
        }

        // 移除所有旧标签
        for (BackpackTabWidget tab : oldTabs) {
            ((ScreenInvoker) screen).invokeRemoveWidget(tab);
        }

        // 创建新标签
        for (int i = 0; i < stacks.size(); i++) {
            int x = left - (i + 1) * 20 + currentOffsetX;
            int y = baseY;
            boolean isSelected = (i == selected);
            ItemStack icon = stacks.get(i);
            int idx = i;
            BackpackTabWidget tab = new BackpackTabWidget(x, y, icon, isSelected, () -> {
                if (mc.player.getInventory() instanceof IExtendedInventory extInv) {
                    extInv.yyzsbackpack$switchToBackpack(idx);
                }
                ClientPlayNetworking.send(new SwitchBackpackC2SPacket(idx));
            });
            ((ScreenInvoker) screen).invokeAddRenderableWidget(tab);
        }
    }

    /**
     * 在背包界面顶部绘制多背包标签（tabs）。
     * 如果当前屏幕实现了 BackpackVisibilityHandler 且背包不可见，则不绘制。
     *
     * @param screen   AbstractContainerScreen 实例
     * @param graphics GuiGraphicsExtractor
     * @param mouseX   鼠标 X 坐标
     * @param mouseY   鼠标 Y 坐标
     */
    public static void drawBackpackTabs(AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 可见性检查
        if (screen instanceof IBackpackToggle handler && !handler.yyzsbackpack$isBackpackVisible()) {
            return;
        }

        List<ItemStack> stacks = BackpackSlotHelper.getAllBackpackStacks(minecraft.player);
        if (stacks.isEmpty()) return;
        int selected = BackpackSlotHelper.getSelectedIndex(minecraft.player);
        if (selected >= stacks.size()) selected = 0;

        int left = ((ScreenAccessor<?>) screen).getLeftPos();
        int top = ((ScreenAccessor<?>) screen).getTopPos();
        int tabHeight = 20;
        int y = top - tabHeight - 2;

        for (int i = 0; i < stacks.size(); i++) {
            int x = left + i * 30;
            boolean isSelected = (i == selected);
            // 绘制背景
            graphics.fill(x, y, x + 28, y + tabHeight, isSelected ? 0xFF_AAAAAA : 0xFF_666666);
            // 绘制物品
            ItemStack stack = stacks.get(i);
            graphics.item(stack, x + 6, y + 2);
            // 悬停提示
            if (mouseX >= x && mouseX < x + 28 && mouseY >= y && mouseY < y + tabHeight) {
                graphics.setTooltipForNextFrame(minecraft.font, stack.getHoverName(), mouseX, mouseY);
            }
        }
    }

    /**
     * 在指定屏幕添加背包开关按钮
     *
     * @param screen 目标容器屏幕
     */
    public static void addBackpackToggle(AbstractContainerScreen<?> screen) {
        // 查找已有的 BackpackToggleButton
        Optional<BackpackToggleButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackToggleButton)
                .map(w -> (BackpackToggleButton) w)
                .findFirst();

        int leftPos = ((ScreenAccessor<?>) screen).getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).getTopPos();
        int x = leftPos + 3;
        int y = topPos + 5;

        if (existing.isPresent()) {
            BackpackToggleButton btn = existing.get();
            btn.setPosition(x, y);   // 更新位置
        } else {
            BackpackToggleButton btn = new BackpackToggleButton(x, y, (IBackpackToggle) screen);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    // ---------- 辅助方法 ----------

    private static BackpackData getBackpackData(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            return backpackItem.getData();
        }
        return null;
    }

    private static int getOffsetX(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) {
            return provider.yyzsbackpack$getBackpackOffsetX();
        }
        return 0;
    }

    private static int getOffsetY(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) {
            return provider.yyzsbackpack$getBackpackOffsetY();
        }
        return 0;
    }

    public static void addBackpackScrollbar(AbstractContainerScreen<?> screen) {
        if (!(screen instanceof IBackpackScroll scrollable)) return;

        // 移除已有的滚动条
        screen.children().removeIf(w -> w instanceof BackpackScrollbar);

        // 获取背包背景边界，计算滚动条位置（右侧）
        Rectangle bounds = getBackpackBackgroundBounds(screen);
        if (bounds == null) return;

        int scrollbarX = bounds.x + bounds.width - 6; // 背景右侧+2像素间隙
        int scrollbarY = bounds.y + 10;
        int scrollbarWidth = 1;
        int scrollbarHeight = bounds.height-20;

        BackpackScrollbar scrollbar = new BackpackScrollbar(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, screen, scrollable);
        ((ScreenInvoker) screen).invokeAddRenderableWidget(scrollbar);
    }
}