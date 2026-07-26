package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.IScreenType;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HorseInventoryScreen.class)
public class HorseInventoryScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "HorseInventoryScreen";
    }

}
