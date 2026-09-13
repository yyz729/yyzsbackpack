package com.yyz.yyzsbackpack.mixin.minecraft.container.furnace;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastFurnaceScreen.class)
public class BlastFurnaceScreenMixin implements IBackpackScreen {

    @Override
    public String yyzsbackpack$getScreenType() {
        return "BlastFurnaceScreen";
    }
}
