package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.api.IBackpackOffsetProvider;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookMixin implements IBackpackOffsetProvider {

    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Override
    public int yyzsbackpack$getBackpackOffsetX() {
        if (recipeBookComponent.isVisible()) {
            return -180;
        }
        return 0;
    }
    @Override
    public int yyzsbackpack$getBackpackOffsetY() {
        return 0;
    }
}