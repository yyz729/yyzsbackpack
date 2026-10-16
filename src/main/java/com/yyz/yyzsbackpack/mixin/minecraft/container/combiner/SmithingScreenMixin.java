package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmithingScreen.class)
public class SmithingScreenMixin implements IBackpackScreen {
    @Override
    public String yyzsbackpack$getScreenType() {
        return "SmithingScreen";
    }
}
