package com.yyz.yyzsbackpack.api.helper;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackOffsetProvider;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.data.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.data.LayoutOrder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import com.yyz.yyzsbackpack.network.SwitchBackpackC2SPacket;
import com.yyz.yyzsbackpack.client.gui.BackpackTabWidget;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenInvoker;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
import java.util.stream.Collectors;

public final class BackpackScreenHelper {

    private BackpackScreenHelper() {
    }

    /**
     * 根据背包数据重新计算所有背包槽位的实际显示位置，并设置到对应的 Slot 中。
     *
     * @param screen 目标容器屏幕
     */
    public static void setupBackpackSlots(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        int start = BackpackMenuHelper.getBackpackSlotStart(menu);
        if (start < 0) {
            return; // 该容器没有添加背包槽位
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null) {
            return;
        }

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        // 遍历所有布局段，计算每个槽位的最终坐标
        for (LayoutSegment segment : data.segments()) {
            int segStart = segment.startSlot();          // 段内起始偏移
            int count = segment.getSlotCount();          // 段内槽位数量
            int baseX = segment.getEffectiveStartX();    // 段基准 X
            int baseY = segment.getEffectiveStartY();    // 段基准 Y
            int columns = segment.columns().orElse(0);
            int rows = segment.rows().orElse(0);
            LayoutOrder order = segment.order();

            List<Slot> slots = menu.slots;

            if (order == LayoutOrder.CUSTOM) {
                // 自定义坐标模式
                List<BackpackSlotPos> customPositions = segment.customPositions()
                        .orElseThrow(() -> new IllegalStateException("Missing customPositions for CUSTOM layout"));
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slots.size()) {
                        break;
                    }
                    BackpackSlotPos pos = customPositions.get(j);
                    Slot slot = slots.get(slotIndex);
                    ((SlotAccessor) slot).setX(pos.x() + offsetX);
                    ((SlotAccessor) slot).setY(pos.y() + offsetY);
                }
            } else {
                for (int j = 0; j < count; j++) {
                    int slotIndex = start + segStart + j;
                    if (slotIndex >= slots.size()) {
                        break;
                    }
                    int relX, relY;
                    relX = j % columns;
                    relY = j / columns;

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
     *
     * @param screen   目标容器屏幕
     * @param graphics GuiGraphicsExtractor 实例
     * @param mouseX   鼠标 X 坐标
     * @param mouseY   鼠标 Y 坐标
     * @param partialTick 部分帧时间
     */
    public static void renderBackpackBackground(AbstractContainerScreen<?> screen,
                                                GuiGraphicsExtractor graphics,
                                                int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);

        BackpackData data = getBackpackData(backpackStack);

        if(data != null) {
            Backpack.LOGGER.info(data.toString());
        }
        if (data == null || data.guiTexture() == null) {
            return;
        }

        // 读取纹理的原始宽度和高度（用于 blit 的尺寸参数）
        int texWidth, texHeight;
        try {
            Resource resource = minecraft.getResourceManager().getResource(data.guiTexture()).orElseThrow();
            try (NativeImage image = NativeImage.read(resource.open())) {
                texWidth = image.getWidth();
                texHeight = image.getHeight();
            }
        } catch (Exception e) {
            // 纹理加载失败时静默返回，避免崩溃
            return;
        }

        int offsetX = getOffsetX(screen);
        int offsetY = getOffsetY(screen);

        // 计算绘制位置：leftPos + backgroundX 偏移，topPos + backgroundY 偏移
        int x = ((ScreenAccessor<?>)screen).getLeftPos() + data.backgroundX() + offsetX;
        int y = ((ScreenAccessor<?>)screen).getTopPos() + data.backgroundY() + offsetY;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,   // 渲染管线
                data.guiTexture(),              // 纹理资源位置
                x, y,                           // 屏幕绘制坐标
                0.0F, 0.0F,                     // 纹理采样起始 UV
                texWidth, texHeight,            // 绘制宽度和高度
                texWidth, texHeight             // 纹理总尺寸（用于 UV 缩放）
        );
    }

    /**
     * 计算当前背包背景纹理在屏幕上的矩形区域。
     * @param screen 当前容器屏幕
     * @return 矩形对象，若无背包背景则返回 null
     */
    @Nullable
    public static Rectangle getBackpackBackgroundBounds(AbstractContainerScreen<?> screen) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return null;

        ItemStack backpackStack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
        if (data == null || data.guiTexture() == null) return null;

        // 获取纹理尺寸（与 renderBackpackBackground 中一致）
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

        // 计算绘制起点（屏幕坐标）
        int x = ((ScreenAccessor<?>) screen).getLeftPos() + data.backgroundX() + offsetX;
        int y = ((ScreenAccessor<?>) screen).getTopPos() + data.backgroundY() + offsetY;

        // 矩形范围：从 (x, y) 到 (x + texWidth, y + texHeight)
        return new Rectangle(x, y, texWidth, texHeight);
    }

    public static void rebuildBackpackTabs(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 获取旧标签列表
        List<BackpackTabWidget> oldTabs = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .map(w -> (BackpackTabWidget) w)
                .toList();

        // 获取当前背包状态
        List<ItemStack> stacks = BackpackSlotHelper.getAllBackpackStacks(mc.player);
        if (stacks.isEmpty()) {
            // 无背包，但旧标签存在则需要移除
            if (!oldTabs.isEmpty()) {
                for (BackpackTabWidget tab : oldTabs) {
                    ((ScreenInvoker) screen).invokeRemoveWidget(tab);
                }
            }
            return;
        }
        int selected = BackpackSlotHelper.getSelectedIndex(mc.player);
        if (selected >= stacks.size()) selected = 0;

        // 检查是否一致
        boolean same = true;
        if (oldTabs.size() != stacks.size()) {
            same = false;
        } else {
            for (int i = 0; i < oldTabs.size(); i++) {
                BackpackTabWidget oldTab = oldTabs.get(i);
                // 比较图标
                if (!ItemStack.matches(oldTab.getIcon(), stacks.get(i))) {
                    same = false;
                    break;
                }
                // 比较选中状态
                if (oldTab.isSelected() != (i == selected)) {
                    same = false;
                    break;
                }
            }
        }

        if (same) {
            return; // 完全一致，不执行任何操作
        }

        // 不一致，移除所有旧标签
        for (BackpackTabWidget tab : oldTabs) {
            ((ScreenInvoker) screen).invokeRemoveWidget(tab);
        }

        // 重建新标签
        int left = ((ScreenAccessor<?>) screen).getLeftPos();
        int top = ((ScreenAccessor<?>) screen).getTopPos();
        int tabHeight = 20;
        int y = top - tabHeight - 2;

        for (int i = 0; i < stacks.size(); i++) {
            int x = left + i * 30;
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

    /**
     * 在背包界面顶部绘制多背包标签（tabs）。
     *
     * @param screen   AbstractContainerScreen 实例
     * @param graphics GuiGraphicsExtractor
     * @param mouseX   鼠标 X 坐标
     * @param mouseY   鼠标 Y 坐标
     */
    public static void drawBackpackTabs(AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

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
}