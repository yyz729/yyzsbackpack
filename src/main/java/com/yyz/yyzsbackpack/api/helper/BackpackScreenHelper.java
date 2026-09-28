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
import com.yyz.yyzsbackpack.config.BackpackConfigs;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackOffsetConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import com.yyz.yyzsbackpack.api.enums.ButtonMode;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenInvoker;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class BackpackScreenHelper {

    private BackpackScreenHelper() {}

    private static final float TITLE_SCROLL_SPEED = 5.0f;
    private static final float STOP_DURATION = 0.8f;
    private static final int   RIGHT_PADDING = 2;

    // ============================================================
    // 分段偏移辅助
    // ============================================================

    /** 屏幕自身提供的全局偏移（IBackpackOffset）。 */
    public static int getOffsetX(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) return provider.yyzsbackpack$getBackpackOffsetX();
        return 0;
    }

    public static int getOffsetY(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffset provider) return provider.yyzsbackpack$getBackpackOffsetY();
        return 0;
    }

    /** 屏幕自身提供的指定分段偏移（IBackpackOffset），默认回退到全局偏移。 */
    public static int getOffsetX(AbstractContainerScreen<?> screen, int segmentIndex) {
        if (screen instanceof IBackpackOffset provider) return provider.yyzsbackpack$getBackpackOffsetX(segmentIndex);
        return 0;
    }

    public static int getOffsetY(AbstractContainerScreen<?> screen, int segmentIndex) {
        if (screen instanceof IBackpackOffset provider) return provider.yyzsbackpack$getBackpackOffsetY(segmentIndex);
        return 0;
    }

    /** 计算指定分段的 UI 偏移 {x, y}。
     *  <p>语义：分段最终位置 = 分段自身 startX/startY（槽位网格基准） + backgroundX/backgroundY（背景微调）
     *  + 本方法返回的偏移。UI 配置里每段占 2 个条目：[目标位置 off, 锚点百分比 anchor]（越界回退段 0）。
     *  <p>锚点以默认值 (100, 0) 为基准做<b>相对</b>调整，保证旧配置（startX + backgroundX + 纹理宽 = 0
     *  这类“右边缘对齐容器左缘”的写法）行为完全不变，同时 startX/backgroundX 直接生效：
     *  <ul>
     *    <li>anchorX 从 100 调小 → 分段相对右移 (100 - anchorX)% × 纹理宽；</li>
     *    <li>anchorY 从 0 调大 → 分段相对上移 anchorY% × 纹理高。</li>
     *  </ul>
     *  <p>注意：不能把锚点算在“含 startX/backgroundX 的绝对边界”上再减掉，否则 startX 会被抵消、
     *  backgroundX 反向作用到槽位、多段布局所有段被钉到同一位置。 */
    private static int[] getUiOffsetForSegment(AbstractContainerScreen<?> screen, int segmentIndex) {
        List<int[]> list = getUiOffsetList(screen);
        if (list.isEmpty()) return new int[]{0, 0};

        int[] off    = BackpackUiConfig.offsetOf(list, segmentIndex);
        int[] anchor = BackpackUiConfig.anchorOf(list, segmentIndex);

        int[] extent = getSegmentExtent(screen, segmentIndex);
        if (extent == null) return new int[]{off[0], off[1]};

        int adjX = (int) Math.round((100 - anchor[0]) / 100.0 * extent[0]);
        int adjY = (int) Math.round((0    - anchor[1]) / 100.0 * extent[1]);
        return new int[]{off[0] + adjX, off[1] + adjY};
    }

    /** 分段“尺寸”{宽, 高}：优先背景纹理尺寸，其次自定义坐标范围，最后槽位网格。锚点调整量按此计算。 */
    @Nullable
    private static int[] getSegmentExtent(AbstractContainerScreen<?> screen, int segmentIndex) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;

        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null || segmentIndex < 0 || segmentIndex >= data.segments().size()) return null;

        LayoutSegment seg = data.segments().get(segmentIndex);
        Minecraft mc = Minecraft.getInstance();

        if (seg.backgroundTexture().isPresent()) {
            Dimension texSize = getTextureSize(mc, seg.backgroundTexture().get());
            if (texSize != null && texSize.width > 0 && texSize.height > 0) {
                return new int[]{texSize.width, texSize.height};
            }
        }

        if (seg.order() == LayoutOrder.CUSTOM) {
            List<BackpackSlotPos> positions = seg.customPositions().orElse(null);
            if (positions == null || positions.isEmpty()) return null;
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (BackpackSlotPos p : positions) {
                minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
            }
            return new int[]{maxX - minX + 18, maxY - minY + 18};
        }

        int columns = seg.columns().orElse(1);
        int rows = seg.rows().orElse(9);
        return new int[]{columns * 18, rows * 18};
    }

    /** 向后兼容：段 0 的 UI 偏移 X / Y。 */
    public static int getUiOffsetX(AbstractContainerScreen<?> screen) {
        return getUiOffsetForSegment(screen, 0)[0];
    }
    public static int getUiOffsetY(AbstractContainerScreen<?> screen) {
        return getUiOffsetForSegment(screen, 0)[1];
    }

    // ============================================================
    // 槽位布局
    // ============================================================

    public static void setupBackpackSlots(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        int start = BackpackMenuHelper.getBackpackSlotStart(menu);
        if (start < 0) return;

        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) {
            for (int i = start; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot instanceof BackpackSlot) {
                    ((SlotAccessor) slot).setX(-1000);
                    ((SlotAccessor) slot).setY(-1000);
                }
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

        int slotCount = menu.slots.size();

        List<LayoutSegment> segments = data.segments();
        int segmentCount = segments.size();

        for (int segIdx = 0; segIdx < segmentCount; segIdx++) {
            LayoutSegment seg = segments.get(segIdx);
            int[] uiOff = getUiOffsetForSegment(screen, segIdx);
            int offsetX = getOffsetX(screen, segIdx) + uiOff[0];
            int offsetY = getOffsetY(screen, segIdx) + uiOff[1];

            int segStart = seg.startSlot();
            int count = seg.getSlotCount();
            int baseX = seg.getEffectiveStartX() + offsetX;
            int baseY = seg.getEffectiveStartY() + offsetY;
            int columns = seg.columns().orElse(1);
            LayoutOrder order = seg.order();

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

            int visibleRows = seg.rows().orElse(9);
            int maxRow = (maxY - minY) / 18;
            int maxScroll = Math.max(0, maxRow - visibleRows + 1);
            scrollable.yyzsbackpack$setSegmentMaxScrollOffset(segIdx, maxScroll);

            int scrollOffset = scrollable.yyzsbackpack$getSegmentScrollOffset(segIdx);
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll;
                scrollable.yyzsbackpack$setSegmentScrollOffset(segIdx, scrollOffset);
            }
            int pixelOffset = scrollOffset * 18;

            int visibleTop = minY;
            int visibleBottom = visibleTop + visibleRows * 18;

            for (int j = 0; j < count; j++) {
                int slotIndex = start + segStart + j;
                if (slotIndex >= slotCount) break;
                Slot slot = menu.slots.get(slotIndex);
                if (!(slot instanceof BackpackSlot)) continue;
                int originalX = origX[slotIndex];
                int originalY = origY[slotIndex];
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
    }

    // ============================================================
    // 背景绘制
    // ============================================================

    public static void addBackpackBackground(AbstractContainerScreen<?> screen,
                                             GuiGraphicsExtractor graphics,
                                             int mouseX, int mouseY, float partialTick) {
        if (screen instanceof IBackpackVisible handler && !handler.yyzsbackpack$isBackpackVisible()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) return;

        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();

        List<LayoutSegment> segments = data.segments();
        for (int segIdx = 0; segIdx < segments.size(); segIdx++) {
            LayoutSegment seg = segments.get(segIdx);
            if (seg.backgroundTexture().isEmpty()) continue;

            int[] uiOff = getUiOffsetForSegment(screen, segIdx);
            int offsetX = getOffsetX(screen, segIdx) + uiOff[0];
            int offsetY = getOffsetY(screen, segIdx) + uiOff[1];

            Identifier tex = seg.backgroundTexture().get();
            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;

            int bgOffX = seg.backgroundX().orElse(0);
            int bgOffY = seg.backgroundY().orElse(0);

            int x = segStartX + bgOffX;
            int y = segStartY + bgOffY;

            Dimension texSize = getTextureSize(minecraft, tex);
            if (texSize == null || texSize.width == 0 || texSize.height == 0) continue;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    tex,
                    x, y,
                    0.0F, 0.0F,
                    texSize.width, texSize.height,
                    texSize.width, texSize.height
            );
        }
    }

    @Nullable
    public static Rectangle getBackpackBackgroundBounds(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackVisible handler && !handler.yyzsbackpack$isBackpackVisible()) return null;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return null;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) return null;

        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        boolean hasAny = false;

        List<LayoutSegment> segments = data.segments();
        for (int segIdx = 0; segIdx < segments.size(); segIdx++) {
            LayoutSegment seg = segments.get(segIdx);
            if (seg.backgroundTexture().isEmpty()) continue;

            int[] uiOff = getUiOffsetForSegment(screen, segIdx);
            int offsetX = getOffsetX(screen, segIdx) + uiOff[0];
            int offsetY = getOffsetY(screen, segIdx) + uiOff[1];

            Identifier tex = seg.backgroundTexture().get();
            Dimension texSize = getTextureSize(minecraft, tex);
            if (texSize == null || texSize.width == 0 || texSize.height == 0) continue;

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int bgOffX = seg.backgroundX().orElse(0);
            int bgOffY = seg.backgroundY().orElse(0);

            int x = segStartX + bgOffX;
            int y = segStartY + bgOffY;
            int w = texSize.width;
            int h = texSize.height;

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + w);
            maxY = Math.max(maxY, y + h);
            hasAny = true;
        }

        if (!hasAny) return null;
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    // ============================================================
    // 页签 / 滚动条 / 标题
    // ============================================================

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

        ItemStack selectedBackpack = BackpackSlotHelper.getSelectedBackpack(mc.player);
        BackpackData data = getBackpackData(selectedBackpack);
        int total = stacks.size();
        int maxVisible = (data != null && data.maxVisibleTabs() > 0) ? data.maxVisibleTabs() : total;
        maxVisible = Math.min(maxVisible, total);

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

        int left = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int top = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();
        // 页签不分段，统一使用段 0 的 UI 偏移，保持原行为
        int[] seg0Ui = getUiOffsetForSegment(screen, 0);
        int offsetX = getOffsetX(screen) + seg0Ui[0];
        int offsetY = getOffsetY(screen) + seg0Ui[1];

        int tabHeight = 18;
        int baseY = top - tabHeight - 2 + offsetY;
        int start = scrollOffset;
        int end = Math.min(start + maxVisible, total);

        boolean needRebuild = false;
        if (oldTabs.size() != (end - start)) {
            needRebuild = true;
        } else {
            for (int j = 0; j < oldTabs.size(); j++) {
                int actualIndex = start + j;
                BackpackTabWidget oldTab = oldTabs.get(j);
                if (!ItemStack.matches(oldTab.getIcon(), stacks.get(actualIndex))) { needRebuild = true; break; }
                if (oldTab.isSelected() != (actualIndex == selected)) { needRebuild = true; break; }
                int expectedX = left - (j + 1) * 9 + offsetX - 7;
                int expectedY = baseY + 25;
                if (oldTab.getX() != expectedX || oldTab.getY() != expectedY) { needRebuild = true; break; }
            }
        }

        if (!needRebuild) return;

        for (BackpackTabWidget tab : oldTabs) {
            ((ScreenInvoker) screen).invokeRemoveWidget(tab);
        }

        for (int j = 0; j < end - start; j++) {
            int actualIndex = start + j;
            int x = left - (j + 1) * 9 + offsetX - 7;
            int y = baseY + 25;
            boolean isSelected = (actualIndex == selected);
            ItemStack icon = stacks.get(actualIndex);
            int idx = actualIndex;
            BackpackTabWidget tab = new BackpackTabWidget(x, y, icon, isSelected, () -> {
                if (mc.player.getInventory() instanceof IExtendedInventory extInv) {
                    extInv.yyzsbackpack$switchToBackpack(idx);
                }
                ClientPacketDistributor.sendToServer(new SwitchBackpackC2SPacket(idx));
            });
            ((ScreenInvoker) screen).invokeAddRenderableWidget(tab);
        }
    }

    public static void addBackpackScrollbar(AbstractContainerScreen<?> screen) {
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
        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();

        List<BackpackScrollWidget> existingBars = screen.children().stream()
                .filter(w -> w instanceof BackpackScrollWidget)
                .map(w -> (BackpackScrollWidget) w).toList();

        List<ScrollbarInfo> expectedInfos = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int[] uiOff = getUiOffsetForSegment(screen, i);
            int offsetX = getOffsetX(screen, i) + uiOff[0];
            int offsetY = getOffsetY(screen, i) + uiOff[1];

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

        if (same) return;

        for (BackpackScrollWidget bar : existingBars) {
            ((ScreenInvoker) screen).invokeRemoveWidget(bar);
        }

        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int[] uiOff = getUiOffsetForSegment(screen, i);
            int offsetX = getOffsetX(screen, i) + uiOff[0];
            int offsetY = getOffsetY(screen, i) + uiOff[1];

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

    private record ScrollbarInfo(int x, int y, int width, int height, int segmentIndex) {}

    public static void addBackpackTitle(AbstractContainerScreen<?> screen,
                                        GuiGraphicsExtractor graphics,
                                        float partialTick) {
        boolean visible = !(screen instanceof IBackpackVisible handler) || handler.yyzsbackpack$isBackpackVisible();
        if (!visible) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null || data.segments().isEmpty()) return;

        Font font = mc.font;
        String title = backpackStack.getHoverName().getString();

        // 标题使用段 0 的偏移
        int[] seg0Ui = getUiOffsetForSegment(screen, 0);
        int offsetX = getOffsetX(screen) + seg0Ui[0];
        int offsetY = getOffsetY(screen) + seg0Ui[1];
        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();

        LayoutSegment firstSeg = data.segments().get(0);
        int segStartX = leftPos + firstSeg.getEffectiveStartX() + offsetX;
        int segStartY = topPos + firstSeg.getEffectiveStartY() + offsetY;

        int baseX, baseY, areaWidth, areaHeight;
        if (firstSeg.backgroundTexture().isPresent()) {
            int bgOffX = firstSeg.backgroundX().orElse(0);
            int bgOffY = firstSeg.backgroundY().orElse(0);
            Dimension texSize = getTextureSize(mc, firstSeg.backgroundTexture().get());
            if (texSize == null || texSize.width == 0 || texSize.height == 0) {
                int cols = firstSeg.columns().orElse(1);
                int rows = firstSeg.rows().orElse(9);
                areaWidth = cols * 18; areaHeight = rows * 18;
                baseX = segStartX; baseY = segStartY;
            } else {
                baseX = segStartX + bgOffX;
                baseY = segStartY + bgOffY;
                areaWidth = texSize.width; areaHeight = texSize.height;
            }
        } else {
            int cols = firstSeg.columns().orElse(1);
            int rows = firstSeg.rows().orElse(9);
            areaWidth = cols * 18; areaHeight = rows * 18;
            baseX = segStartX; baseY = segStartY;
        }

        int textX = baseX + 7;
        int textY = baseY + 5;

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
            rightBoundary = baseX + areaWidth - 7 - RIGHT_PADDING;
        } else {
            int lastJ = visibleTabCount - 1;
            int tabLeftX = leftPos - (lastJ + 1) * 9 + offsetX - 7;
            rightBoundary = tabLeftX - RIGHT_PADDING;
        }

        int availableWidth = rightBoundary - textX;
        if (availableWidth <= 0) return;

        int titleWidth = font.width(title);

        if (titleWidth <= availableWidth) {
            graphics.text(font, title, textX, textY, -12566464, false);
            return;
        }

        int maxScroll = titleWidth - availableWidth;
        float moveTime = maxScroll / TITLE_SCROLL_SPEED;
        float halfCycle = moveTime + STOP_DURATION;
        float totalCycle = halfCycle * 2;

        float elapsed = (System.currentTimeMillis() % 100000L) / 1000.0f;
        float t = elapsed % totalCycle;
        float offset;

        if (t < STOP_DURATION) offset = 0;
        else if (t < halfCycle) offset = ((t - STOP_DURATION) / moveTime) * maxScroll;
        else if (t < halfCycle + STOP_DURATION) offset = maxScroll;
        else offset = maxScroll * (1.0f - (t - halfCycle - STOP_DURATION) / moveTime);

        int drawX = textX + availableWidth - titleWidth + (int) offset;

        graphics.enableScissor(textX, textY, textX + availableWidth, textY + font.lineHeight);
        graphics.text(font, title, drawX, textY, -12566464, false);
        graphics.disableScissor();
    }

    // ============================================================
    // 纹理尺寸 & 命中检测
    // ============================================================

    private static final Map<Identifier, Dimension> TEXTURE_SIZE_CACHE = new HashMap<>();
    private static Dimension getTextureSize(Minecraft mc, Identifier texId) {
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

    public static int getSegmentAtPosition(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return -1;
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null) return -1;

        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();

        List<LayoutSegment> segments = data.segments();
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int[] uiOff = getUiOffsetForSegment(screen, i);
            int offsetX = getOffsetX(screen, i) + uiOff[0];
            int offsetY = getOffsetY(screen, i) + uiOff[1];

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int width = seg.columns().get() * 18;
            int height = seg.rows().get() * 18;

            Rectangle rect = new Rectangle(segStartX, segStartY, width, height);
            if (rect.contains(mouseX, mouseY)) return i;
        }
        return -1;
    }

    // ============================================================
    // 配置偏移（供外部调用）
    // ============================================================

    public static int getConfigOffsetX(AbstractContainerScreen<?> screen, int defaultX) {
        return getConfigOffsetX(screen, defaultX, 0);
    }

    public static int getConfigOffsetX(AbstractContainerScreen<?> screen, int defaultX, int segmentIndex) {
        Map<String, List<int[]>> offsetMap = BackpackConfigs.offset();
        if (offsetMap == null) return defaultX;
        String screenType = getScreenType(screen);
        List<int[]> offsets = offsetMap.get(screenType);
        if (offsets == null || offsets.isEmpty()) return defaultX;
        int[] v = BackpackOffsetConfig.offsetFor(offsetMap, screenType, segmentIndex);
        return v.length > 0 ? v[0] : defaultX;
    }

    public static int getConfigOffsetY(AbstractContainerScreen<?> screen, int defaultY) {
        return getConfigOffsetY(screen, defaultY, 0);
    }

    public static int getConfigOffsetY(AbstractContainerScreen<?> screen, int defaultY, int segmentIndex) {
        Map<String, List<int[]>> offsetMap = BackpackConfigs.offset();
        if (offsetMap == null) return defaultY;
        String screenType = getScreenType(screen);
        List<int[]> offsets = offsetMap.get(screenType);
        if (offsets == null || offsets.isEmpty()) return defaultY;
        int[] v = BackpackOffsetConfig.offsetFor(offsetMap, screenType, segmentIndex);
        return v.length > 1 ? v[1] : defaultY;
    }

    // ============================================================
    // 单段 / 全背包边界
    // ============================================================

    /** 单段边界 {minX, minY, W, H}，以容器左上角为原点。 */
    private static int[] computeSingleSegmentBounds(Minecraft mc, LayoutSegment seg) {
        int segStartX = seg.getEffectiveStartX();
        int segStartY = seg.getEffectiveStartY();
        int count = seg.getSlotCount();
        int columns = seg.columns().orElse(1);
        LayoutOrder order = seg.order();

        // 优先用背景纹理作为该段的边界
        if (seg.backgroundTexture().isPresent()) {
            Identifier tex = seg.backgroundTexture().get();
            Dimension texSize = getTextureSize(mc, tex);
            if (texSize != null && texSize.width > 0 && texSize.height > 0) {
                int bgX = seg.backgroundX().orElse(0);
                int bgY = seg.backgroundY().orElse(0);
                return new int[]{
                        segStartX + bgX,
                        segStartY + bgY,
                        texSize.width,
                        texSize.height
                };
            }
        }

        // 回退：用格子范围
        if (order == LayoutOrder.CUSTOM) {
            List<BackpackSlotPos> positions = seg.customPositions().orElse(null);
            if (positions == null) return new int[]{0, 0, 0, 0};
            int limit = Math.min(count, positions.size());
            if (limit <= 0) return new int[]{0, 0, 0, 0};
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (int j = 0; j < limit; j++) {
                BackpackSlotPos p = positions.get(j);
                int x = segStartX + p.x();
                int y = segStartY + p.y();
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x + 18);
                maxY = Math.max(maxY, y + 18);
            }
            return new int[]{minX, minY, maxX - minX, maxY - minY};
        }

        int rows = (count + columns - 1) / columns;
        return new int[]{segStartX, segStartY, columns * 18, rows * 18};
    }

    private static int[] computeSegmentBounds(Player player, int segmentIndex) {
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null) return new int[]{0, 0, 0, 0};

        List<LayoutSegment> segments = data.segments();
        if (segmentIndex < 0 || segmentIndex >= segments.size()) return new int[]{0, 0, 0, 0};

        return computeSingleSegmentBounds(Minecraft.getInstance(), segments.get(segmentIndex));
    }

    /** 全背包合并边界（保留供扩展）。 */
    private static int[] computeBackpackBounds(Player player) {
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpack);
        if (data == null) return new int[]{0, 0, 0, 0};

        Minecraft mc = Minecraft.getInstance();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        boolean hasAny = false;

        for (LayoutSegment seg : data.segments()) {
            int[] b = computeSingleSegmentBounds(mc, seg);
            if (b[2] <= 0 || b[3] <= 0) continue;
            minX = Math.min(minX, b[0]);
            minY = Math.min(minY, b[1]);
            maxX = Math.max(maxX, b[0] + b[2]);
            maxY = Math.max(maxY, b[1] + b[3]);
            hasAny = true;
        }

        if (!hasAny) return new int[]{0, 0, 0, 0};
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    // ============================================================
    // 屏幕类型 / UI 配置查询
    // ============================================================

    private static List<int[]> getUiOffsetList(AbstractContainerScreen<?> screen) {
        Map<String, List<int[]>> uiMap = BackpackConfigs.ui();
        if (uiMap == null) return List.of();
        List<int[]> list = uiMap.get(getScreenType(screen));
        return list != null ? list : List.of();
    }

    private static String getScreenType(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackScreen provider) return provider.yyzsbackpack$getScreenType();
        return screen.getClass().getSimpleName();
    }

    // ============================================================
    // 控制按钮（保持原样，不分段）
    // ============================================================

    public static void addBackpackMoveIToBButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveIBButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveIBButton)
                .map(w -> (BackpackMoveIBButton) w)
                .findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveIBButton(x, y)); }
    }

    public static void addBackpackMoveBToIButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveBIButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveBIButton)
                .map(w -> (BackpackMoveBIButton) w)
                .findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveBIButton(x, y)); }
    }

    public static void addBackpackSortButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackSortButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackSortButton)
                .map(w -> (BackpackSortButton) w)
                .findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackSortButton(x, y)); }
    }

    public static void addBackpackVisibleButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackVisibleButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackVisibleButton)
                .map(w -> (BackpackVisibleButton) w)
                .findFirst();
        if (existing.isPresent()) { existing.get().setPosition(x, y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackVisibleButton(x, y, (IBackpackVisible) screen)); }
    }

    public static void addBackpackControls(AbstractContainerScreen<?> screen) { addBackpackControls(screen, 0); }
    public static void addBackpackControls(AbstractContainerScreen<?> screen, int extraYOffset) {
        addBackpackControls(screen, extraYOffset, true, false);
    }

    public static void addBackpackControls(AbstractContainerScreen<?> screen, int extraYOffset, boolean applyToGroup1, boolean applyToGroup2) {
        BackpackMainConfig config = Backpack.getMainConfig();
        ButtonMode mode = config.button;

        Player player = Minecraft.getInstance().player;
        if (player == null) { removeAllBackpackControlButtons(screen); return; }

        boolean showButtons = switch (mode) {
            case HIDE -> false;
            case SHOW -> true;
            case AUTO -> !BackpackSlotHelper.getAllBackpackStacks(player).isEmpty();
        };

        if (!showButtons) { removeAllBackpackControlButtons(screen); return; }

        String screenType = getScreenType(screen);
        Map<String, List<int[]>> controlMap = BackpackConfigs.control();
        if (controlMap == null) return;
        List<int[]> offsets = controlMap.get(screenType);
        if (offsets == null) return;

        int leftPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getLeftPos();
        int topPos = ((ScreenAccessor<?>) screen).yyzsbackpack_getTopPos();
        int size = 6;
        int gap = 2;

        for (int groupIdx = 0; groupIdx < offsets.size(); groupIdx++) {
            int[] offset = offsets.get(groupIdx);
            int offsetX = offset[0];
            int offsetY = offset[1];

            int finalExtraOffset = 0;
            if (groupIdx == 0 && applyToGroup1) finalExtraOffset = extraYOffset;
            else if (groupIdx == 1 && applyToGroup2) finalExtraOffset = extraYOffset;

            int toggleX = leftPos + offsetX;
            int toggleY = topPos - size + offsetY + finalExtraOffset;

            if (groupIdx == 0) {
                addBackpackVisibleButton(screen, toggleX, toggleY);
                addBackpackSortButton(screen, toggleX + size + gap - 1, toggleY);
                addBackpackMoveBToIButton(screen, toggleX + 2 * (size + gap), toggleY);
                addBackpackMoveIToBButton(screen, toggleX + 3 * (size + gap), toggleY);
            } else if (groupIdx == 1) {
                addBackpackMoveBToCButton(screen, toggleX, toggleY);
                addBackpackMoveCToBButton(screen, toggleX + size + gap, toggleY);
                addBackpackMoveIToCButton(screen, toggleX + 2 * (size + gap), toggleY);
                addBackpackMoveCToIButton(screen, toggleX + 3 * (size + gap), toggleY);
            }
        }
    }

    private static void removeAllBackpackControlButtons(AbstractContainerScreen<?> screen) {
        List<Class<?>> buttonClasses = List.of(
                BackpackVisibleButton.class, BackpackSortButton.class,
                BackpackMoveBIButton.class, BackpackMoveIBButton.class,
                BackpackMoveBCButton.class, BackpackMoveCBButton.class,
                BackpackMoveICButton.class, BackpackMoveCIButton.class);
        for (var child : screen.children().stream().filter(w -> buttonClasses.stream().anyMatch(c -> c.isInstance(w))).toList()) {
            ((ScreenInvoker) screen).invokeRemoveWidget(child);
        }
    }

    public static boolean handleMouseScrolled(AbstractContainerScreen<?> screen,
                                              double mouseX, double mouseY,
                                              double scrollX, double scrollY) {
        for (var child : screen.children()) {
            if (child instanceof BackpackSortButton btn) {
                if (btn.isMouseOver(mouseX, mouseY)) { BackpackSortButton.cycleAlgorithm(); return true; }
            }
        }

        int segmentIndex = BackpackScreenHelper.getSegmentAtPosition(screen, mouseX, mouseY);
        if (segmentIndex >= 0) {
            IBackpackScroll scrollable = (IBackpackScroll) screen;
            int delta = (int) Math.signum(scrollY);
            int oldOffset = scrollable.yyzsbackpack$getSegmentScrollOffset(segmentIndex);
            scrollable.yyzsbackpack$setSegmentScrollOffset(segmentIndex, oldOffset - delta);
            return true;
        }

        boolean mouseOverTab = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .anyMatch(w -> w.isMouseOver(mouseX, mouseY));
        if (mouseOverTab) {
            IBackpackTabScroll tabScroll = (IBackpackTabScroll) screen;
            int delta = (int) Math.signum(scrollY);
            int oldOffset = tabScroll.yyzsbackpack$getTabScrollOffset();
            tabScroll.yyzsbackpack$setTabScrollOffset(oldOffset - delta);
            BackpackScreenHelper.addBackpackTabs(screen);
            return true;
        }

        return false;
    }

    private static void addBackpackMoveBToCButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveBCButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveBCButton)
                .map(w -> (BackpackMoveBCButton) w).findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveBCButton(x, y)); }
    }

    private static void addBackpackMoveCToBButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveCBButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveCBButton)
                .map(w -> (BackpackMoveCBButton) w).findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveCBButton(x, y)); }
    }

    private static void addBackpackMoveIToCButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveICButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveICButton)
                .map(w -> (BackpackMoveICButton) w).findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveICButton(x, y)); }
    }

    private static void addBackpackMoveCToIButton(AbstractContainerScreen<?> screen, int x, int y) {
        Optional<BackpackMoveCIButton> existing = screen.children().stream()
                .filter(w -> w instanceof BackpackMoveCIButton)
                .map(w -> (BackpackMoveCIButton) w).findFirst();
        if (existing.isPresent()) { existing.get().setX(x); existing.get().setY(y); }
        else { ((ScreenInvoker) screen).invokeAddRenderableWidget(new BackpackMoveCIButton(x, y)); }
    }

    public static BackpackData getBackpackData(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem backpackItem) return backpackItem.getData();
        return null;
    }
}