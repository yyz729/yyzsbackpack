package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.api.IBackpackOffset;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookMixin implements IBackpackOffset {

    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Override
    public int yyzsbackpack$getBackpackOffsetX() {
        if (recipeBookComponent.isVisible()) {
            AbstractRecipeBookScreen<?> screen = (AbstractRecipeBookScreen<?>) (Object) this;
            return BackpackScreenHelper.getConfigOffsetX(screen, 0);
        }
        return 0;
    }
    @Override
    public int yyzsbackpack$getBackpackOffsetY() {
        if (recipeBookComponent.isVisible()) {
            AbstractRecipeBookScreen<?> screen = (AbstractRecipeBookScreen<?>) (Object) this;
            return BackpackScreenHelper.getConfigOffsetY(screen, 0);
        }
        return 0;
    }
}