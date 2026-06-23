package com.yyz.yyzsbackpack.mixin.minecraft;

import com.mojang.blaze3d.platform.NativeImage;
import com.yyz.yyzsbackpack.api.LayoutOrder;
import com.yyz.yyzsbackpack.api.BackpackSlotPos;
import com.yyz.yyzsbackpack.api.LayoutSegment;
import com.yyz.yyzsbackpack.api.inventory.IBackpackMenu;
import com.yyz.yyzsbackpack.api.inventory.IExtendedInventory;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.SlotAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
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
        InventoryMenu menu = ((InventoryScreen)(Object)this).getMenu();

        int start = ((IBackpackMenu) menu).yyzsbackpack$getBackpackSlotStart();
        if (start == 0) return;

        Player player = minecraft.player;
        if (player == null) return;

        ItemStack backpackStack = getEquippedBackpack(player);

        IExtendedInventory extInv = (IExtendedInventory) player.getInventory();
        extInv.yyzsbackpack$syncFromBackpack(backpackStack);

        BackpackData data = null;
        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
            data = backpackItem.getData();
        }

        if (data != null) {
            for (LayoutSegment segment : data.segments()) {
                int segStart = segment.startSlot();
                int count = segment.getSlotCount();
                int startX = segment.getEffectiveStartX();
                int startY = segment.getEffectiveStartY();
                int columns = segment.columns().orElse(0);
                int rows = segment.rows().orElse(0);
                LayoutOrder order = segment.order();

                if (order == LayoutOrder.CUSTOM) {
                    List<BackpackSlotPos> positions = segment.customPositions()
                            .orElseThrow(() -> new IllegalStateException("missing customPositions"));
                    for (int j = 0; j < count; j++) {
                        int slotIndex = start + segStart + j;
                        BackpackSlotPos pos = positions.get(j);
                        ((SlotAccessor) menu.slots.get(slotIndex)).setX(pos.x());
                        ((SlotAccessor) menu.slots.get(slotIndex)).setY(pos.y());
                    }
                } else {
                    for (int j = 0; j < count; j++) {
                        int relX = j % columns;
                        int relY = j / columns;
                        int x = startX + relX * 18;
                        int y = startY + relY * 18;
                        int slotIndex = start + segStart + j;
                        ((SlotAccessor) menu.slots.get(slotIndex)).setX(x);
                        ((SlotAccessor) menu.slots.get(slotIndex)).setY(y);
                    }
                }
            }
        }
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


        Player player = minecraft.player;
        if (player == null) return;

        ItemStack backpackStack = getEquippedBackpack(player);
        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) return;

        BackpackData data = backpackItem.getData();
        if (data == null || data.guiTexture() == null) return;

        int texWidth, texHeight;
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(data.guiTexture()).orElseThrow();
            try (NativeImage image = NativeImage.read(resource.open())) {
                texWidth = image.getWidth();
                texHeight = image.getHeight();
            }
        } catch (Exception e) {
            return;
        }

        int x = leftPos + data.backgroundX();
        int y = topPos + data.backgroundY();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                data.guiTexture(),
                x, y,
                0.0F, 0.0F,
                texWidth, texHeight,
                texWidth, texHeight
        );
    }

    @Unique
    private static ItemStack getEquippedBackpack(Player player) {
        ItemStack mainHand = player.getItemBySlot(EquipmentSlot.CHEST);
        if (mainHand.getItem() instanceof BackpackItem) {
            return mainHand;
        }
        return ItemStack.EMPTY;
    }
}