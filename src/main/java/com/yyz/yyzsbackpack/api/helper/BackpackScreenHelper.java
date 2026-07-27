package com.yyz.yyzsbackpack.api.helper;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.*;
import com.yyz.yyzsbackpack.api.data.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.data.LayoutOrder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import com.yyz.yyzsbackpack.client.gui.widget.control.*;
import com.yyz.yyzsbackpack.client.gui.widget.layout.BackpackScrollWidget;
import com.yyz.yyzsbackpack.client.gui.widget.layout.BackpackTabWidget;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenInvoker;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class BackpackScreenHelper {

    private BackpackScreenHelper() {}

    private static final float TITLE_SCROLL_SPEED = 5.0f;
    private static final float STOP_DURATION = 0.8f;       // 两端停留时间（秒）
    private static final int   RIGHT_PADDING = 2;          // 右侧安全间距，防止文字紧贴标签
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
        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) {
            for (int i = start; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                ((SlotAccessor) slot).setX(-1000);
                ((SlotAccessor) slot).setY(-1000);
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) return;

        if (!(screen instanceof IBackpackScroll scrollable)) return;

        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);
        int slotCount = menu.slots.size();

        // 获取所有段的列表
        List<LayoutSegment> segments = data.segments();
        int segmentCount = segments.size();

        // 对每个段单独处理
        for (int segIdx = 0; segIdx < segmentCount; segIdx++) {
            LayoutSegment seg = segments.get(segIdx);
            int segStart = seg.startSlot();
            int count = seg.getSlotCount();
            int baseX = seg.getEffectiveStartX() + offsetX;
            int baseY = seg.getEffectiveStartY() + offsetY;
            int columns = seg.columns().orElse(1);
            LayoutOrder order = seg.order();

            // 收集该段所有槽位的原始坐标，并记录 minY / maxY
            int[] origX = new int[slotCount];
            int[] origY = new int[slotCount];
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            boolean hasSlots = false;

            if (order == LayoutOrder.CUSTOM) {
                List<BackpackSlotPos> customPositions = seg.customPositions()
                        .orElseThrow(() -> new IllegalStateException("Missing customPositions"));
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slotCount) break;
                    BackpackSlotPos pos = customPositions.get(j);
                    int x = pos.x() + offsetX;
                    int y = pos.y() + offsetY;
                    origX[slotIndex] = x;
                    origY[slotIndex] = y;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    hasSlots = true;
                }
            } else {
                // DEFAULT 网格布局
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
                    hasSlots = true;
                }
            }

            if (!hasSlots) continue;

            // 计算该段的最大滚动偏移
            int visibleRows = seg.rows().orElse(9); // 默认9行
            int maxRow = (maxY - minY) / 18;
            int maxScroll = Math.max(0, maxRow - visibleRows + 1);
            scrollable.yyzsbackpack$setSegmentMaxScrollOffset(segIdx, maxScroll);

            // 获取当前偏移并限制
            int scrollOffset = scrollable.yyzsbackpack$getSegmentScrollOffset(segIdx);
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll;
                scrollable.yyzsbackpack$setSegmentScrollOffset(segIdx, scrollOffset);
            }
            int pixelOffset = scrollOffset * 18;

            // 可视区域：该段顶部 minY，向下 visibleRows 行
            int visibleTop = minY;
            int visibleBottom = visibleTop + visibleRows * 18;

            // 应用偏移到该段所有槽位
            for (int j = 0; j < count; j++) {
                int slotIndex = start + segStart + j;
                if (slotIndex >= slotCount) break;
                Slot slot = menu.slots.get(slotIndex);
                int originalX = origX[slotIndex];
                int originalY = origY[slotIndex];
                int newY = originalY - pixelOffset;

                // 是否在可视区域内
                if (newY >= visibleTop - 2 && newY + 16 <= visibleBottom + 2) {
                    ((SlotAccessor) slot).setX(originalX);
                    ((SlotAccessor) slot).setY(newY);
                } else {
                    ((SlotAccessor) slot).setX(-1000);
                    ((SlotAccessor) slot).setY(-1000);
                }
            }
        }
    }

    /**
     * 在屏幕背景之上绘制自定义背包 GUI 纹理。
     * 如果当前屏幕实现了 BackpackVisibilityHandler 且背包不可见，则不绘制。
     *
     * @param screen      目标容器屏幕
     * @param graphics    GuiGraphics 实例
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     * @param partialTick 部分帧时间
     */
    public static void addBackpackBackground(AbstractContainerScreen<?> screen,
                                             GuiGraphics graphics,
                                             int mouseX, int mouseY, float partialTick) {
        // 可见性检查
        if (screen instanceof IBackpackVisible handler && !handler.yyzsbackpack$isBackpackVisible()) {
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

        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);

        int x = ((ScreenAccessor<?>) screen).getLeftPos() + data.backgroundX() + offsetX;
        int y = ((ScreenAccessor<?>) screen).getTopPos() + data.backgroundY() + offsetY;

        graphics.blit(
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
        if (screen instanceof IBackpackVisible handler && !handler.yyzsbackpack$isBackpackVisible()) {
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

        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);

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

        List<BackpackTabWidget> oldTabs = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .map(w -> (BackpackTabWidget) w)
                .toList();

        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        List<ItemStack> stacks = BackpackSlotHelper.getAllBackpackStacks(mc.player);

        if (!visible || stacks.isEmpty()) {
            for (BackpackTabWidget tab : oldTabs) {
                ((ScreenInvoker) screen).invokeRemoveWidget(tab);
            }
            return;
        }

        // 获取数据
        ItemStack selectedBackpack = BackpackSlotHelper.getSelectedBackpack(mc.player);
        BackpackData data = getBackpackData(selectedBackpack);
        int total = stacks.size();
        int maxVisible = (data != null && data.maxVisibleTabs() > 0) ? data.maxVisibleTabs() : total;
        maxVisible = Math.min(maxVisible, total);

        // 获取滚动偏移
        int scrollOffset = 0;
        if (screen instanceof IBackpackTabScroll tabScroller) {
            scrollOffset = tabScroller.yyzsbackpack$getTabScrollOffset();
            int maxOffset = Math.max(0, total - maxVisible);
            if (scrollOffset > maxOffset) {
                scrollOffset = maxOffset;
                tabScroller.yyzsbackpack$setTabScrollOffset(scrollOffset);
            }
        }

        int selected = BackpackSlotHelper.getSelectedIndex(mc.player);
        if (selected >= total) selected = 0;

        int left = ((ScreenAccessor<?>) screen).getLeftPos();
        int top = ((ScreenAccessor<?>) screen).getTopPos();
        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);

        int tabHeight = 18;
        int baseY = top - tabHeight - 2 + offsetY;
        int start = scrollOffset;
        int end = Math.min(start + maxVisible, total);

        // 检查是否需要重建
        boolean needRebuild = false;
        if (oldTabs.size() != (end - start)) {
            needRebuild = true;
        } else {
            for (int j = 0; j < oldTabs.size(); j++) {
                int actualIndex = start + j;
                BackpackTabWidget oldTab = oldTabs.get(j);
                if (!ItemStack.matches(oldTab.getIcon(), stacks.get(actualIndex))) {
                    needRebuild = true;
                    break;
                }
                if (oldTab.isSelected() != (actualIndex == selected)) {
                    needRebuild = true;
                    break;
                }
                // 位置计算基于可见顺序 j
                int expectedX = left - (j + 1) * 9 + offsetX - 7;
                int expectedY = baseY + 25;
                if (oldTab.getX() != expectedX || oldTab.getY() != expectedY) {
                    needRebuild = true;
                    break;
                }
            }
        }

        if (!needRebuild) return;

        // 移除旧标签
        for (BackpackTabWidget tab : oldTabs) {
            ((ScreenInvoker) screen).invokeRemoveWidget(tab);
        }

        // 创建新标签
        for (int j = 0; j < end - start; j++) {
            int actualIndex = start + j;
            int x = left - (j + 1) * 9 + offsetX - 7;   // 基于可见顺序 j，固定位置
            int y = baseY + 25;
            boolean isSelected = (actualIndex == selected);
            ItemStack icon = stacks.get(actualIndex);
            int idx = actualIndex;
            BackpackTabWidget tab = new BackpackTabWidget(x, y, icon, isSelected, () -> {
                if (mc.player.getInventory() instanceof IExtendedInventory extInv) {
                    extInv.yyzsbackpack$switchToBackpack(idx);
                }
                ClientPlayNetworking.send(new SwitchBackpackC2SPacket(idx));
            });
            ((ScreenInvoker) screen).invokeAddRenderableWidget(tab);
        }
    }

    public static void addBackpackScrollbar(AbstractContainerScreen<?> screen) {
        // 可见性检查
        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) {
            for (BackpackScrollWidget bar : screen.children().stream()
                    .filter(w -> w instanceof BackpackScrollWidget)
                    .map(w -> (BackpackScrollWidget) w).toList()) {
                ((ScreenInvoker) screen).invokeRemoveWidget(bar);
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            for (BackpackScrollWidget bar : screen.children().stream()
                    .filter(w -> w instanceof BackpackScrollWidget)
                    .map(w -> (BackpackScrollWidget) w).toList()) {
                ((ScreenInvoker) screen).invokeRemoveWidget(bar);
            }
            return;
        }

        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null) {
            for (BackpackScrollWidget bar : screen.children().stream()
                    .filter(w -> w instanceof BackpackScrollWidget)
                    .map(w -> (BackpackScrollWidget) w).toList()) {
                ((ScreenInvoker) screen).invokeRemoveWidget(bar);
            }
            return;
        }

        List<LayoutSegment> segments = data.segments();
        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);
        int leftPos = ((ScreenAccessor<?>) screen).getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).getTopPos();

        List<BackpackScrollWidget> existingBars = screen.children().stream()
                .filter(w -> w instanceof BackpackScrollWidget)
                .map(w -> (BackpackScrollWidget) w)
                .toList();

        List<ScrollbarInfo> expectedInfos = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int columns = seg.columns().get();
            int visibleRows = seg.rows().get();

            int segWidth = columns * 18;
            int segHeight = visibleRows * 18;

            int scrollbarX = segStartX + segWidth;
            int scrollbarY = segStartY + 2;
            int scrollbarWidth = 2;
            int scrollbarHeight = segHeight - 4;

            expectedInfos.add(new ScrollbarInfo(scrollbarX, scrollbarY,
                    scrollbarWidth, scrollbarHeight, i));
        }

        boolean same = true;
        if (existingBars.size() != expectedInfos.size()) {
            same = false;
        } else {
            for (int i = 0; i < existingBars.size(); i++) {
                BackpackScrollWidget bar = existingBars.get(i);
                ScrollbarInfo info = expectedInfos.get(i);
                if (bar.getX() != info.x || bar.getY() != info.y ||
                        bar.getWidth() != info.width || bar.getHeight() != info.height ||
                        bar.getSegmentIndex() != info.segmentIndex) {
                    same = false;
                    break;
                }
            }
        }

        if (same) {
            return;
        }

        for (BackpackScrollWidget bar : existingBars) {
            ((ScreenInvoker) screen).invokeRemoveWidget(bar);
        }

        // 创建并添加新滚动条
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int columns = seg.columns().get();
            int visibleRows = seg.rows().get();

            int segWidth = columns * 18;
            int segHeight = visibleRows * 18;

            int scrollbarX = segStartX + segWidth;
            int scrollbarY = segStartY + 2;
            int scrollbarWidth = 2;
            int scrollbarHeight = segHeight - 4;

            BackpackScrollWidget scrollbar = new BackpackScrollWidget(
                    scrollbarX, scrollbarY,
                    scrollbarWidth, scrollbarHeight,
                    screen, (IBackpackScroll) screen, i
            );
            ((ScreenInvoker) screen).invokeAddRenderableWidget(scrollbar);
        }
    }

    // 辅助记录预期滚动条信息
        private record ScrollbarInfo(int x, int y, int width, int height, int segmentIndex) {
    }

    public static void addBackpackTitle(AbstractContainerScreen<?> screen,
                                        GuiGraphics graphics,
                                        float partialTick) {
        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null || data.guiTexture() == null) return;

        Font font = mc.font;
        String title = backpackStack.getHoverName().getString();

        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);
        int leftPos = ((ScreenAccessor<?>) screen).getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).getTopPos();

        int bgX = leftPos + data.backgroundX() + offsetX;
        int bgY = topPos + data.backgroundY() + offsetY;
        int textX = bgX + 7;
        int textY = bgY + 5;

        // 计算右边界（最左侧标签左边缘 - 安全间距）
        List<ItemStack> allBackpacks = BackpackSlotHelper.getAllBackpackStacks(player);
        int totalTabs = allBackpacks.size();
        int maxVisibleTabs = (data.maxVisibleTabs() > 0) ? data.maxVisibleTabs() : totalTabs;
        maxVisibleTabs = Math.min(maxVisibleTabs, totalTabs);
        int scrollOffset = 0;
        if (screen instanceof IBackpackTabScroll tabScroller) {
            scrollOffset = tabScroller.yyzsbackpack$getTabScrollOffset();
            int maxOffset = Math.max(0, totalTabs - maxVisibleTabs);
            if (scrollOffset > maxOffset) scrollOffset = maxOffset;
        }
        int visibleTabCount = Math.min(maxVisibleTabs, totalTabs - scrollOffset);

        int rightBoundary;
        if (visibleTabCount == 0) {
            Dimension texSize = getTextureSize(mc, data.guiTexture());
            if (texSize == null) return;
            rightBoundary = bgX + texSize.width - 7 - RIGHT_PADDING; // 背景右内边距 - 安全间距
        } else {
            int lastJ = visibleTabCount - 1;
            int tabLeftX = leftPos - (lastJ + 1) * 9 + offsetX - 7;
            rightBoundary = tabLeftX - RIGHT_PADDING; // 标签左边缘再向左留出空隙
        }

        int availableWidth = rightBoundary - textX;
        if (availableWidth <= 0) return;

        int titleWidth = font.width(title);

        if (titleWidth <= availableWidth) {
            // 文字不超宽，静态绘制
            graphics.drawString(font, title, textX, textY, -12566464, false);
            return;
        }

        //带停留的来回滚动
        int maxScroll = titleWidth - availableWidth;                     // 需要滚动的最大像素
        float moveTime = maxScroll / TITLE_SCROLL_SPEED;                 // 单向移动耗时（秒）
        float halfCycle = moveTime + STOP_DURATION;                      // 半周期：移动 + 停留
        float totalCycle = halfCycle * 2;                                // 完整周期：去 + 回

        float elapsed = (System.currentTimeMillis() % 100000L) / 1000.0f;
        float t = elapsed % totalCycle;                                  // 当前周期内时间
        float offset;

        if (t < STOP_DURATION) {
            offset = 0;                                                  // 右端停留（文字右对齐）
        } else if (t < halfCycle) {
            float moveProgress = (t - STOP_DURATION) / moveTime;
            offset = moveProgress * maxScroll;                           // 向右 → 左移动
        } else if (t < halfCycle + STOP_DURATION) {
            offset = maxScroll;                                          // 左端停留（文字左对齐）
        } else {
            float moveProgress = (t - halfCycle - STOP_DURATION) / moveTime;
            offset = maxScroll * (1.0f - moveProgress);                  // 向左 → 右移动
        }

        int drawX = textX + availableWidth - titleWidth + (int) offset;

        graphics.enableScissor(textX, textY, textX + availableWidth, textY + font.lineHeight);
        graphics.drawString(font, title, drawX, textY, -12566464, false);
        graphics.disableScissor();
    }

    // 纹理尺寸缓存
    private static final Map<ResourceLocation, Dimension> TEXTURE_SIZE_CACHE = new HashMap<>();
    private static Dimension getTextureSize(Minecraft mc, ResourceLocation texId) {
        return TEXTURE_SIZE_CACHE.computeIfAbsent(texId, id -> {
            try {
                Resource resource = mc.getResourceManager().getResource(id).orElseThrow();
                try (NativeImage image = NativeImage.read(resource.open())) {
                    return new Dimension(image.getWidth(), image.getHeight());
                }
            } catch (Exception e) {
                return new Dimension(0, 0);
            }
        });
    }


    /**
     * 添加移动物品按钮到指定屏幕的指定位置。
     * 如果按钮已存在则更新位置，否则创建新按钮。
     *
     * @param screen 目标容器屏幕
     * @param x     按钮的 X 坐标（左上角）
     * @param y     按钮的 Y 坐标（左上角）
     */
    public static void addBackpackMoveIToBButton(AbstractContainerScreen<?> screen, int x, int y) {

        // 查找已有排序按钮
        Optional<BackpackMoveIBButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveIBButton)
                .map(w -> (BackpackMoveIBButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveIBButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveIBButton btn = new BackpackMoveIBButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    /**
     * 添加移动物品按钮到指定屏幕的指定位置。
     * 如果按钮已存在则更新位置，否则创建新按钮。
     *
     * @param screen 目标容器屏幕
     * @param x     按钮的 X 坐标（左上角）
     * @param y     按钮的 Y 坐标（左上角）
     */
    public static void addBackpackMoveBToIButton(AbstractContainerScreen<?> screen, int x, int y) {

        // 查找已有排序按钮
        Optional<BackpackMoveBIButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveBIButton)
                .map(w -> (BackpackMoveBIButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveBIButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveBIButton btn = new BackpackMoveBIButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }
    /**
     * 添加背包整理按钮到指定屏幕的指定位置。
     * 如果按钮已存在则更新位置，否则创建新按钮。
     *
     * @param screen 目标容器屏幕
     * @param x     按钮的 X 坐标（左上角）
     * @param y     按钮的 Y 坐标（左上角）
     */
    public static void addBackpackSortButton(AbstractContainerScreen<?> screen, int x, int y) {

        Optional<BackpackSortButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackSortButton)
                .map(w -> (BackpackSortButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackSortButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackSortButton btn = new BackpackSortButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    public static void addBackpackVisibleButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackVisibleButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackVisibleButton)
                .map(w -> (BackpackVisibleButton) w)
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setPosition(x, y);
        } else {
            BackpackVisibleButton btn = new BackpackVisibleButton(x, y, (IBackpackVisible) screen);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }
    public static void addBackpackControls(AbstractContainerScreen<?> screen) {
        String screenType = getScreenType(screen);
        List<int[]> offsets = Backpack.getControlConfig().getControlPoss().get(screenType);
        if (offsets == null) return;

        int leftPos = ((ScreenAccessor<?>) screen).getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).getTopPos();
        int size = 6;
        int gap = 2;

        // 遍历每个组
        for (int groupIdx = 0; groupIdx < offsets.size(); groupIdx++) {
            int[] offset = offsets.get(groupIdx);
            int offsetX = offset[0];
            int offsetY = offset[1];

            // 计算该组起始坐标
            int toggleX = leftPos +  offsetX;
            int toggleY = topPos - size + offsetY;

            if (groupIdx == 0) {
                addBackpackVisibleButton(screen, toggleX, toggleY);
                addBackpackSortButton(screen, toggleX + size + gap - 1, toggleY);
                addBackpackMoveBToIButton(screen, toggleX + 2*(size + gap), toggleY);
                addBackpackMoveIToBButton(screen, toggleX + 3*(size + gap), toggleY);
            } else if (groupIdx == 1) {
                addBackpackMoveBToCButton(screen, toggleX, toggleY);
                addBackpackMoveCToBButton(screen, toggleX + size + gap, toggleY);
                addBackpackMoveIToCButton(screen, toggleX + 2*(size + gap), toggleY);
                addBackpackMoveCToIButton(screen, toggleX + 3*(size + gap), toggleY);

            }
        }
    }

    private static void addBackpackMoveBToCButton(AbstractContainerScreen<?> screen, int x, int y) {

        Optional<BackpackMoveBCButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveBCButton)
                .map(w -> (BackpackMoveBCButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveBCButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveBCButton btn = new BackpackMoveBCButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    private static void addBackpackMoveCToBButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveCBButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveCBButton)
                .map(w -> (BackpackMoveCBButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveCBButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveCBButton btn = new BackpackMoveCBButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    private static void addBackpackMoveIToCButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveICButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveICButton)
                .map(w -> (BackpackMoveICButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveICButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveICButton btn = new BackpackMoveICButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    private static void addBackpackMoveCToIButton(AbstractContainerScreen<?> screen, int x, int y) {

        Optional<BackpackMoveCIButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveCIButton)
                .map(w -> (BackpackMoveCIButton) w)
                .findFirst();

        if (existing.isPresent()) {
            BackpackMoveCIButton btn = existing.get();
            btn.setX(x);
            btn.setY(y);
        } else {
            BackpackMoveCIButton btn = new BackpackMoveCIButton(x, y);
            ((ScreenInvoker) screen).invokeAddRenderableWidget(btn);
        }
    }

    // 根据鼠标位置确定所在的段索引
    public static int getSegmentAtPosition(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return -1;
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null) return -1;

        int offsetX = getOffsetX(screen) + getUiOffsetX(screen);
        int offsetY = getOffsetY(screen) + getUiOffsetY(screen);
        int leftPos = ((ScreenAccessor<?>) screen).getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).getTopPos();

        List<LayoutSegment> segments = data.segments();
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int width = seg.columns().get() * 18;
            int height = seg.rows().get() * 18; // 可视区域高度

            // 扩大一点点击区域，方便操作
            Rectangle rect = new Rectangle(segStartX, segStartY, width, height);
            if (rect.contains(mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    public static BackpackData getBackpackData(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            return backpackItem.getData();
        }
        return null;
    }

    public static int getOffsetX(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) {
            return provider.yyzsbackpack$getBackpackOffsetX();
        }
        return 0;
    }

    public static int getOffsetY(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) {
            return provider.yyzsbackpack$getBackpackOffsetY();
        }
        return 0;
    }

    private static int[] getUiOffset(AbstractContainerScreen<?> screen) {
        BackpackUiConfig config = Backpack.getUiConfig();
        if (config == null) return new int[]{0, 0};
        String screenType = getScreenType(screen);
        List<int[]> offsets = config.getUiOffsets().get(screenType);
        if (offsets == null || offsets.isEmpty()) return new int[]{0, 0};
        return offsets.getFirst();
    }

    private static int getUiOffsetX(AbstractContainerScreen<?> screen) {
        return getUiOffset(screen)[0];
    }

    private static int getUiOffsetY(AbstractContainerScreen<?> screen) {
        return getUiOffset(screen)[1];
    }

    private static String getScreenType(AbstractContainerScreen<?> screen) {
        if (screen instanceof IScreenType provider) {
            return provider.yyzsbackpack$getScreenType();
        }
        return screen.getClass().getSimpleName();
    }

}