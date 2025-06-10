package com.yyz.yyzsbackpack.mixin.screen;

import com.yyz.yyzsbackpack.BackpackRenderCondition;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceRenderConditionMixin <T extends AbstractFurnaceScreenHandler> extends HandledScreen<T> {

    public AbstractFurnaceRenderConditionMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Shadow public abstract RecipeBookWidget getRecipeBookWidget();


    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(CallbackInfo ci) {
        ((BackpackRenderCondition)handler).setRenderBackpack(!getRecipeBookWidget().isOpen());
    }

}
