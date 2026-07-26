package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IScreenType;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmokerScreen.class)
public class SmokerScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "SmokerScreen";
    }
}
