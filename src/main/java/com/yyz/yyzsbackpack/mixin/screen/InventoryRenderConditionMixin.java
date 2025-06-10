package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.yyz.yyzsbackpack.BackpackManager.SLOT_TEXTURE;

@Mixin(InventoryScreen.class)
public  abstract class InventoryRenderConditionMixin extends AbstractInventoryScreen<PlayerScreenHandler>{



    @Shadow public abstract RecipeBookWidget getRecipeBookWidget();

    @Shadow @Final private RecipeBookWidget recipeBook;

    public InventoryRenderConditionMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void renderForeground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        context.drawTexture(SLOT_TEXTURE,  x + 8 + 69 -1,  y + 8 - 1 + 18 * 2, 0, 0, 18, 18, 18, 18);

    }


    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(!recipeBook.isOpen());
    }
}
