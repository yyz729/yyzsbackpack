package com.yyz.yyzsbackpack.neoforge;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class BackpackPlayerRecipeTransferHandler implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {
    private static final IntSet PLAYER_INV_INDEXES = IntArraySet.of(new int[]{0, 1, 3, 4});
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> handler;

    public BackpackPlayerRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
        IRecipeTransferInfo<InventoryMenu, RecipeHolder<CraftingRecipe>> basicRecipeTransferInfo = handlerHelper.createBasicRecipeTransferInfo(InventoryMenu.class, (MenuType)null, RecipeTypes.CRAFTING, 1, 4, 9, 36+54);
        this.handler = handlerHelper.createUnregisteredRecipeTransferHandler(basicRecipeTransferInfo);
    }

    public Class<? extends InventoryMenu> getContainerClass() {
        return this.handler.getContainerClass();
    }

    public Optional<MenuType<InventoryMenu>> getMenuType() {
        return this.handler.getMenuType();
    }

    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    public @Nullable IRecipeTransferError transferRecipe(InventoryMenu container, RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!this.handlerHelper.recipeTransferHasServerSupport()) {
            Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.no.server");
            return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        } else {
            List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
            if (!validateIngredientsOutsidePlayerGridAreEmpty(slotViews)) {
                Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
                return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
            } else {
                List<IRecipeSlotView> filteredSlotViews = filterSlots(slotViews);
                IRecipeSlotsView filteredRecipeSlots = this.handlerHelper.createRecipeSlotsView(filteredSlotViews);
                return this.handler.transferRecipe(container, recipe, filteredRecipeSlots, player, maxTransfer, doTransfer);
            }
        }
    }

    private static boolean validateIngredientsOutsidePlayerGridAreEmpty(List<IRecipeSlotView> slotViews) {
        int bound = slotViews.size();

        for(int i = 0; i < bound; ++i) {
            if (!PLAYER_INV_INDEXES.contains(i)) {
                IRecipeSlotView slotView = (IRecipeSlotView)slotViews.get(i);
                if (!slotView.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static List<IRecipeSlotView> filterSlots(List<IRecipeSlotView> slotViews) {
        IntStream var10000 = PLAYER_INV_INDEXES.intStream();
        Objects.requireNonNull(slotViews);
        return var10000.mapToObj(slotViews::get).toList();
    }
}