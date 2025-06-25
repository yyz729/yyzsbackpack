package com.yyz.yyzsbackpack.compat.emi;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.BackpackExclusionZoneProvider;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.*;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.handler.CookingRecipeHandler;
import dev.emi.emi.handler.CraftingRecipeHandler;
import dev.emi.emi.handler.InventoryRecipeHandler;
import dev.emi.emi.handler.StonecuttingRecipeHandler;

import me.shedaniel.math.Rectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EmiEntrypoint
public class BackpackEmiPlugin implements EmiPlugin {

    @Override
    public void initialize(EmiInitRegistry registry) {
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof AbstractContainerScreen<?> handledScreen) {
                if (handledScreen instanceof BackpackExclusionZoneProvider provider) {
                    for (Rect2i zone : provider.getBackpackExclusionZones()) {
                        consumer.accept(new Bounds(
                                zone.getX(),
                                zone.getY(),
                                zone.getWidth(),
                                zone.getHeight()
                        ));
                    }
                }
            }
        });
    }

}