package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.IScreenType;
import net.minecraft.client.gui.screens.inventory.NautilusInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NautilusInventoryScreen.class)
public class NautilusInventoryScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "NautilusInventoryScreen";
    }

}
