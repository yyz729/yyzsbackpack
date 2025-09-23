package com.yyz.yyzsbackpack.forge.mixin.compat.quark;

import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.client.BackpackRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen;

@Mixin(BackpackInventoryScreen.class)
public abstract class BackpackInventoryScreenMixin extends InventoryScreen {


    public BackpackInventoryScreenMixin(Player arg) {
        super(arg);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderForeground(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
        BackpackMenu condition = (BackpackMenu) menu;
        condition.setBackpackEquipSlotPos(0, -29);
        BackpackRenderer.renderEquipSlotBackground(menu,guiGraphics,leftPos + 8 + 69 -1,  topPos + 8 - 1 + 18 * 2);

    }
}
