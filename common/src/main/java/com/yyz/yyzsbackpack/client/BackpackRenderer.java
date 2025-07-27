package com.yyz.yyzsbackpack.client;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackpackRenderer {
    public static final ResourceLocation BACKPACK_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack.png");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/slot.png");

    public static void renderEquipSlotBackground(InventoryMenu menu, GuiGraphics guiGraphics, int x, int y){
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) return;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,SLOT_TEXTURE,  x + ((BackpackMenu)menu).getBackpackEquipSlotX(),  y+ ((BackpackMenu)menu).getBackpackEquipSlotY(), 0, 0, 18, 18, 18, 18);

    }

    // 背景渲染方法 - 添加偏移值支持
    public static void renderEquippedBackpackBackground(GuiGraphics context, int x, int y,
                                                        int backgroundWidth, int backgroundHeight,
                                                        Inventory inventory, boolean shouldRenderBackpack,
                                                        BackpackMenu renderCondition) {


        if (!shouldRenderBackpack) return;

        int columns = 0;
        ItemStack stack = BackpackPlatform.getEquipped(inventory.player);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        // 应用偏移值
        int left = x - 14 - columns * 18 - 1 + renderCondition.getBackpackGuiX();
        int top = y + (backgroundHeight - 174) / 2 + renderCondition.getBackpackGuiY();
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
    }

    public static void renderBackpackPreview(GuiGraphics guiGraphics, Minecraft minecraft, AbstractContainerMenu menu, @Nullable Slot hoveredSlot, int leftPos, int topPos, int imageWidth, int imageHeight){
        ((BackpackMenu)menu).setPreviewVisible(false);
        boolean requireKey;
        switch (Backpack.getConfig().tooltip_modifier.toLowerCase()) {
            case "shift" -> requireKey = Screen.hasShiftDown();
            case "alt" -> requireKey = Screen.hasAltDown();
            case "ctrl" -> requireKey = Screen.hasControlDown();
            case "none" -> requireKey = true; // 不需要按键
            default -> {
                // 无效配置时使用默认值（shift）
                if (!Screen.hasShiftDown()) return;
                return;
            }
        }

        // 如果配置要求按键但未按下，则返回
        if (!requireKey) return;

        if(hoveredSlot == null || !menu.getCarried().isEmpty()) return;

        ItemStack backpackStack = hoveredSlot.getItem();

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) return;
        BackpackRenderer.renderPreviewBackpackBackground(guiGraphics,backpackStack, leftPos, topPos, imageWidth, imageHeight, (BackpackMenu) menu);
        ((BackpackMenu)menu).setPreviewVisible(true);
        // 从数据组件读取背包内容

        List<ItemStack> backpackItems = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());
        if (backpackItems == null) return;

        // 使用您的位置计算逻辑
        int baseHeight = imageHeight;
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

    public static void renderPreviewBackpackBackground(GuiGraphics context, ItemStack stack, int x, int y,
                                                       int backgroundWidth, int backgroundHeight,
                                                       BackpackMenu renderCondition) {



        int columns = 0;
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        // 应用偏移值
        int left = x - 14 - columns * 18 - 1 + renderCondition.getBackpackGuiX();
        int top = y + (backgroundHeight - 174) / 2 + renderCondition.getBackpackGuiY();
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;
        context.blit(RenderPipelines.GUI_TEXTURED,BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
    }
}
