package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BackpackRenderer {

    public static void renderEquipSlotBackground(InventoryMenu menu, GuiGraphics guiGraphics, int x, int y){
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) return;
        guiGraphics.blit(new ResourceLocation(Backpack.MOD_ID, "textures/gui//slot.png"),  x + ((BackpackMenu)menu).getBackpackEquipSlotX(),  y +((BackpackMenu)menu).getBackpackEquipSlotY() , 0, 0, 18, 18, 18, 18);
    }

    // 背景渲染方法 - 添加偏移值支持
    public static void renderEquippedBackpackBackground(GuiGraphics context, int x, int y,
                                                        int backgroundWidth, int backgroundHeight,
                                                        Inventory inventory, boolean shouldRenderBackpack,
                                                        BackpackMenu renderCondition) {

        RenderSystem.enableBlend();
        if (!shouldRenderBackpack) return;
        int width = 256;
        int height = 256;
        ResourceLocation texture = null;
        ItemStack stack = BackpackPlatform.getEquipped(inventory.player);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            texture = backpackItem.getBackpackType().getGuiTexture();
            width = backpackItem.getBackpackType().guiWidth();
            height = backpackItem.getBackpackType().guiHeight();
        }

        if(texture == null) return;

        // 应用偏移值
        int left = x - width - 1 + renderCondition.getBackpackGuiX();
        int top = y + (backgroundHeight - height) / 2 + renderCondition.getBackpackGuiY();
        context.blit(texture, left, top, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }

    public static void renderBackpackPreview(GuiGraphics context, Minecraft minecraft, AbstractContainerMenu menu, @Nullable Slot hoveredSlot, int leftPos, int topPos, int imageWidth, int imageHeight){
        RenderSystem.enableBlend();
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
        BackpackRenderer.renderPreviewBackpackBackground(context,backpackStack, leftPos, topPos, imageWidth, imageHeight, (BackpackMenu)menu);
        ((BackpackMenu)menu).setPreviewVisible(true);
        // 从数据组件读取背包内容

        List<ItemStack> backpackItems = new ArrayList<>();
        CompoundTag nbt = backpackStack.getTag();
        if (nbt != null && nbt.contains("BackpackItems", Tag.TAG_LIST)) {

            int numSlots = backpackItem.getBackpackType().getSize();

            // 初始化全空物品列表
            for (int i = 0; i < numSlots; i++) {
                backpackItems.add(ItemStack.EMPTY);
            }

            // 填充非空物品
            ListTag itemsTag = nbt.getList("BackpackItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < itemsTag.size(); i++) {
                CompoundTag itemTag = itemsTag.getCompound(i);
                int slotIndex = itemTag.getInt("Slot");
                if (slotIndex >= 0 && slotIndex < numSlots) {
                    backpackItems.set(slotIndex, ItemStack.of(itemTag));
                }
            }
        }
        if (backpackItems.isEmpty()) return;
        // 使用与实际槽位布局相同的计算逻辑
        int baseHeight = imageHeight;
        int columns = backpackItem.getBackpackType().getColumns();
        int rows = backpackItem.getBackpackType().getRows(); // 使用实际行数而非固定9行

        // 与实际槽位布局相同的坐标计算
        int startX = -25 - (columns-1)*18; // 基础X偏移
        int startY = (baseHeight - 166) / 2 + 3; // 基础Y位置

        for (int row = 0; row < rows; row++) {  // 外层循环行
            for (int column = 0; column < columns; column++) {  // 内层循环列
                int slotIndex = row * columns + column; // 行优先顺序
                if (slotIndex >= backpackItems.size()) continue;

                // 计算每个物品的位置（与实际槽位相同的计算方式）
                int x = leftPos + startX + column * 18;
                int y = topPos + startY + row * 18;
                ItemStack stack = backpackItems.get(slotIndex);
                // 绘制物品图标
                context.renderItem(stack, x, y);
                // 绘制物品数量
                context.renderItemDecorations(minecraft.font, stack, x, y);
            }
        }
        RenderSystem.disableBlend();
    }
    // 背景渲染方法 - 添加偏移值支持
    public static void renderPreviewBackpackBackground(GuiGraphics context, ItemStack stack, int x, int y,
                                                       int backgroundWidth, int backgroundHeight,
                                                       BackpackMenu renderCondition) {



        int width = 256;
        int height = 256;
        ResourceLocation texture = null;
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            texture = backpackItem.getBackpackType().getGuiTexture();
            width = backpackItem.getBackpackType().guiWidth();
            height = backpackItem.getBackpackType().guiHeight();
        }

        if(texture == null) return;

        // 应用偏移值
        int left = x  - width - 1 + renderCondition.getBackpackGuiX();
        int top = y + (backgroundHeight - 174) / 2 + renderCondition.getBackpackGuiY();

        context.blit(texture, left, top, 0, 0, width, height, width, height);
    }
}
