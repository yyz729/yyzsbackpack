package com.yyz.yyzsbackpack.fabric.mixin;

import com.yyz.yyzsbackpack.api.BackpackCondition;
import de.rubixdev.inventorio.client.ui.InventorioScreen;
import de.rubixdev.inventorio.player.InventorioScreenHandler;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventorioScreen.class)
public abstract class InventorioScreenMixin extends EffectRenderingInventoryScreen<InventorioScreenHandler> {
    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    public InventorioScreenMixin(InventorioScreenHandler arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(CallbackInfo ci) {
        ((BackpackCondition)menu).setRenderBackpack(!getRecipeBookComponent().isVisible());
    }
}
