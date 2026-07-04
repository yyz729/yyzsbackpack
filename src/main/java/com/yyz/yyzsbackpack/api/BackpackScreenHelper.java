package com.yyz.yyzsbackpack.api;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.BackpackContainerHelper;
import com.yyz.yyzsbackpack.api.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.LayoutOrder;
import com.yyz.yyzsbackpack.api.LayoutSegment;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
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
     *
     * @param screen 目标容器屏幕
     */
    public static void setupBackpackSlots(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        int start = BackpackContainerHelper.getBackpackSlotStart(menu);
        if (start < 0) {
            return; // 该容器没有添加背包槽位
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack backpackStack = getEquippedBackpack(player);
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
     * 在屏幕背景之上绘制自定义背包 GUI 纹理（背景图）。
     * 应在屏幕的 renderBg() 方法末尾调用。
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

        ItemStack backpackStack = getEquippedBackpack(player);
        BackpackData data = getBackpackData(backpackStack);
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
     * 计算当前背包背景纹理在屏幕上的矩形区域（屏幕坐标）。
     * @param screen 当前容器屏幕
     * @return 矩形对象，若无背包背景则返回 null
     */
    @Nullable
    public static Rectangle getBackpackBackgroundBounds(AbstractContainerScreen<?> screen) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return null;

        ItemStack backpackStack = getEquippedBackpack(player);
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


    private static ItemStack getEquippedBackpack(Player player) {
        return Backpack.getSelectedBackpack(player);
    }

    private static BackpackData getBackpackData(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            return backpackItem.getData();
        }
        return null;
    }

    private static int getOffsetX(AbstractContainerScreen<?> screen) {
        if (screen instanceof BackpackScreenOffsetProvider provider) {
            return provider.yyzsbackpack$getBackpackOffsetX();
        }
        return 0;
    }

    private static int getOffsetY(AbstractContainerScreen<?> screen) {
        if (screen instanceof BackpackScreenOffsetProvider provider) {
            return provider.yyzsbackpack$getBackpackOffsetY();
        }
        return 0;
    }
}