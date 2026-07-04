package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.BackpackScreenOffsetProvider;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin implements BackpackScreenOffsetProvider {

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