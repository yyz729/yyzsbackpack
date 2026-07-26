package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IScreenType;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastFurnaceScreen.class)
public class BlastFurnaceScreenMixin implements IScreenType {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "BlastFurnaceScreen";
    }
}
