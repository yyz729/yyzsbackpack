package com.yyz.yyzsbackpack.forge.mixin;

import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;

import static com.yyz.yyzsbackpack.BackpackManager.SLOT_TEXTURE;

@Mixin(BackpackInventoryScreen.class)
public abstract class BackpackInventoryScreenMixin extends EffectRenderingInventoryScreen<CuriosContainerV2> {

    public BackpackInventoryScreenMixin(CuriosContainerV2 arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderForeground(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
        BackpackRenderCondition condition = (BackpackRenderCondition) menu;
        condition.setEquippackOffset(0, -29);
        guiGraphics.blit(SLOT_TEXTURE,  leftPos + 8 + 69 -1,  topPos + 8 - 1 + 18 * 2, 0, 0, 18, 18, 18, 18);

    }
}
