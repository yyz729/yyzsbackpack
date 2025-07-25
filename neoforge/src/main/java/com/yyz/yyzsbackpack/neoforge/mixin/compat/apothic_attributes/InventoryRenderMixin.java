package com.yyz.yyzsbackpack.neoforge.mixin.compat.apothic_attributes;

import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import dev.shadowsoffire.apothic_attributes.client.AttributesGui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public  abstract class InventoryRenderMixin extends EffectRenderingInventoryScreen<InventoryMenu> {


    public InventoryRenderMixin(InventoryMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void addBackpackSlots(CallbackInfo ci) {
        if(BackpackPlatform.isModLoaded("apothic_attributes")) {
            if (AttributesGui.wasOpen) {
                ((BackpackMenu) menu).setBackpackGuiPos(-131, 0);
            }else {
                ((BackpackMenu) menu).setBackpackGuiPos(0, 0);
            }
        }
    }
}
