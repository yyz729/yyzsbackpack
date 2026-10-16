package com.yyz.yyzsbackpack.mixin.minecraft.container.chest;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "ContainerScreen";
    }
    public ContainerScreenMixin(ChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ContainerScreen screen = (ContainerScreen)(Object)this;
        BackpackScreenHelper.setupBackpackSlots(screen);
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);

        int rows = this.menu.getRowCount();
        int extraYOffset = (rows - 3) * 18;
        BackpackScreenHelper.addBackpackControls(screen, extraYOffset);
    }
}
