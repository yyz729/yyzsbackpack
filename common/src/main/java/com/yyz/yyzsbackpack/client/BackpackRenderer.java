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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackpackRenderer {

    public static void renderEquipSlotBackground(InventoryMenu menu, GuiGraphics guiGraphics, int x, int y){
        if(BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) return;
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/slot.png"),  x + ((BackpackMenu)menu).getBackpackEquipSlotX(),  y +((BackpackMenu)menu).getBackpackEquipSlotY(), 0, 0, 18, 18, 18, 18);
    }

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
}
