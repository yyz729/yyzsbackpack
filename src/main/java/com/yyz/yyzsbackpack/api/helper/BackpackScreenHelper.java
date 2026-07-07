package com.yyz.yyzsbackpack.api.helper;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.api.BackpackVisibilityHandler;
import com.yyz.yyzsbackpack.api.IBackpackOffsetProvider;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.data.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.data.LayoutOrder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import com.yyz.yyzsbackpack.client.gui.BackpackTabWidget;
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
        if (start < 0) {
            return;
        }

        // 检查背包可见性（默认可见）
        boolean visible = screen instanceof BackpackVisibilityHandler handler
                ? handler.yyzsbackpack$isBackpackVisible()
                : true;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        // 不可见：将所有背包槽位移出屏幕
        if (!visible) {
            List<Slot> slots = menu.slots;
            for (int i = start; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                ((SlotAccessor) slot).setX(-1000);
                ((SlotAccessor) slot).setY(-1000);
            }
            return;
        }

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) {
            return;
        }

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        for (LayoutSegment segment : data.segments()) {
            int segStart = segment.startSlot();
            int count = segment.getSlotCount();
            int baseX = segment.getEffectiveStartX();
            int baseY = segment.getEffectiveStartY();
            int columns = segment.columns().orElse(0);
            int rows = segment.rows().orElse(0);
            LayoutOrder order = segment.order();

            List<Slot> slots = menu.slots;

            if (order == LayoutOrder.CUSTOM) {
                List<BackpackSlotPos> customPositions = segment.customPositions()
                        .orElseThrow(() -> new IllegalStateException("Missing customPositions for CUSTOM layout"));
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slots.size()) break;
                    BackpackSlotPos pos = customPositions.get(j);
                    Slot slot = slots.get(slotIndex);
                    ((SlotAccessor) slot).setX(pos.x() + offsetX);
                    ((SlotAccessor) slot).setY(pos.y() + offsetY);
                }
            } else {
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slots.size()) break;
                    int relX = j % columns;
                    int relY = j / columns;
                    int x = baseX + relX * 18 + offsetX;
                    int y = baseY + relY * 18 + offsetY;
                    Slot slot = slots.get(slotIndex);
                    ((SlotAccessor) slot).setX(x);
                    ((SlotAccessor) slot).setY(y);
                }
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
    public static void renderBackpackBackground(AbstractContainerScreen<?> screen,
                                                GuiGraphicsExtractor graphics,
                                                int mouseX, int mouseY, float partialTick) {
        // 可见性检查
        if (screen instanceof BackpackVisibilityHandler handler && !handler.yyzsbackpack$isBackpackVisible()) {
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
        if (screen instanceof BackpackVisibilityHandler handler && !handler.yyzsbackpack$isBackpackVisible()) {
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
    public static void rebuildBackpackTabs(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 获取当前所有标签
        List<BackpackTabWidget> oldTabs = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .map(w -> (BackpackTabWidget) w)
                .toList();

        // 检查可见性
        boolean visible = screen instanceof BackpackVisibilityHandler handler
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
        if (screen instanceof BackpackVisibilityHandler handler && !handler.yyzsbackpack$isBackpackVisible()) {
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

    // ---------- 私有辅助方法 ----------

    private static BackpackData getBackpackData(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            return backpackItem.getData();
        }
        return null;
    }

    private static int getOffsetX(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffsetProvider provider) {
            return provider.yyzsbackpack$getBackpackOffsetX();
        }
        return 0;
    }

    private static int getOffsetY(AbstractContainerScreen<?> screen) {
        if (screen instanceof IBackpackOffsetProvider provider) {
            return provider.yyzsbackpack$getBackpackOffsetY();
        }
        return 0;
    }
}