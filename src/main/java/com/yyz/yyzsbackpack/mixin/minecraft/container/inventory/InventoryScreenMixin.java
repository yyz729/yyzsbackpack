package com.yyz.yyzsbackpack.mixin.minecraft.container.inventory;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.SwitchBackpackC2SPacket;
import com.yyz.yyzsbackpack.api.*;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {

    public InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBookComponent, Inventory inventory, Component title) {
        super(menu, recipeBookComponent, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {

        BackpackScreenHelper.setupBackpackSlots((InventoryScreen) (Object) this);
    }


    @Inject(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onExtractBackgroundInvoke(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {

        BackpackScreenHelper.renderBackpackBackground((InventoryScreen) (Object) this, graphics, mouseX, mouseY, partialTick);

        drawBackpackTabs((InventoryScreen) (Object) this, graphics, mouseX, mouseY);
    }


    @Unique private List<ItemStack> backpackStacks = List.of();
    @Unique
    private int selectedIndex = 0;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // 初始化背包列表
        if (minecraft.player != null) {
            backpackStacks = Backpack.getAllBackpackStacks(minecraft.player);
            selectedIndex = Backpack.getSelectedIndex(minecraft.player);
            if (selectedIndex >= backpackStacks.size()) selectedIndex = 0;
        }
    }

    @Unique
    private void drawBackpackTabs(InventoryScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (backpackStacks.isEmpty()) return;

        int left = ((ScreenAccessor<?>)screen).getLeftPos();
        int top = ((ScreenAccessor<?>)screen).getTopPos();
        int tabHeight = 20;
        int y = top - tabHeight - 2;

        for (int i = 0; i < backpackStacks.size(); i++) {
            int x = left + i * 30;
            boolean selected = (i == selectedIndex);
            // 绘制背景
            graphics.fill(x, y, x + 28, y + tabHeight, selected ? 0xFF_AAAAAA : 0xFF_666666);
            // 绘制物品图标
            ItemStack stack = backpackStacks.get(i);
            graphics.item(stack, x + 6, y + 2);
            // 悬停提示
            if (mouseX >= x && mouseX < x + 28 && mouseY >= y && mouseY < y + tabHeight) {
                graphics.setTooltipForNextFrame(font, stack.getHoverName(), mouseX, mouseY);
            }
        }
    }


    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {

        if (event.button() == 0 && handleTabClick((InventoryScreen) (Object) this, (int) event.x(), (int) event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Unique
    private boolean handleTabClick(InventoryScreen screen, int mouseX, int mouseY) {
        if (backpackStacks.isEmpty()) return false;

        int left = ((ScreenAccessor<?>)screen).getLeftPos();
        int top = ((ScreenAccessor<?>)screen).getTopPos();
        int tabHeight = 20;
        int y = top - tabHeight - 2;

        for (int i = 0; i < backpackStacks.size(); i++) {
            int x = left + i * 30;
            if (mouseX >= x && mouseX < x + 28 && mouseY >= y && mouseY < y + tabHeight) {
                if (i != selectedIndex) {
                    selectedIndex = i;
                    // 调用 Inventory 切换方法
                    if (minecraft.player != null && minecraft.player.getInventory() instanceof IExtendedInventory extInv) {
                        extInv.yyzsbackpack$switchToBackpack(i);
                    }
                    // 通知服务端切换索引
                    ClientPlayNetworking.send(new SwitchBackpackC2SPacket(i));
                }
                return true;
            }
        }
        return false;
    }
}