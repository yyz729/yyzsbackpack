package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FurnaceScreen.class)
public class FurnaceScreenMixin implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "FurnaceScreen";
    }
}
