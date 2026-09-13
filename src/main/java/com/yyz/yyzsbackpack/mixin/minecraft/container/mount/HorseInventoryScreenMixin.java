package com.yyz.yyzsbackpack.mixin.minecraft.container.mount;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HorseInventoryScreen.class)
public class HorseInventoryScreenMixin implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "HorseInventoryScreen";
    }

}
