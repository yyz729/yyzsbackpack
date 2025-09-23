package com.yyz.yyzsbackpack.forge.compat.jei;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.base.BackpackScreen;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class BackpackJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Backpack.MOD_ID, "backpack");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CraftingMenu.class, MenuType.CRAFTING, RecipeTypes.CRAFTING, 1, 9, 10, 36+ BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(FurnaceMenu.class, MenuType.FURNACE, RecipeTypes.SMELTING, 0, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(FurnaceMenu.class, MenuType.FURNACE, RecipeTypes.FUELING, 1, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(SmokerMenu.class, MenuType.SMOKER, RecipeTypes.SMOKING, 0, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(SmokerMenu.class, MenuType.SMOKER, RecipeTypes.FUELING, 1, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(BlastFurnaceMenu.class, MenuType.BLAST_FURNACE, RecipeTypes.BLASTING, 0, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(BlastFurnaceMenu.class, MenuType.BLAST_FURNACE, RecipeTypes.FUELING, 1, 1, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(BrewingStandMenu.class, MenuType.BREWING_STAND, RecipeTypes.BREWING, 0, 4, 5, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(AnvilMenu.class, MenuType.ANVIL, RecipeTypes.ANVIL, 0, 2, 3, 36+BackpackHelper.getMaxBackpackSize());
        registration.addRecipeTransferHandler(SmithingMenu.class, MenuType.SMITHING, RecipeTypes.SMITHING, 0, 3, 3, 36+BackpackHelper.getMaxBackpackSize());
        IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
        BackpackPlayerRecipeTransferHandler recipeTransferHandler = new BackpackPlayerRecipeTransferHandler(transferHelper);
        registration.addRecipeTransferHandler(recipeTransferHandler, RecipeTypes.CRAFTING);
    }

    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new BackpackGuiHandler());
    }


    private static class BackpackGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
        @Nonnull
        @Override
        public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
            if (screen instanceof BackpackScreen provider) {
                return provider.getBackpackExclusionZones(); // 直接返回 Rect2i
            }
            return Collections.emptyList();
        }
    }

}
