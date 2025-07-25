package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class BackpackRenderer {
    public static final ResourceLocation BACKPACK_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack.png");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/slot.png");

    public static void renderEquipSlotBackground(InventoryMenu menu, GuiGraphics guiGraphics, int x, int y){
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().useDedicatedSlot) return;
        guiGraphics.blit(SLOT_TEXTURE,  x + ((BackpackMenu)menu).getBackpackEquipSlotX(),  y +((BackpackMenu)menu).getBackpackEquipSlotY(), 0, 0, 18, 18, 18, 18);
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

        RenderSystem.enableBlend();
        context.blit(BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
        RenderSystem.disableBlend();
    }

    // 背景渲染方法 - 添加偏移值支持
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
        context.blit(BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
    }
}
