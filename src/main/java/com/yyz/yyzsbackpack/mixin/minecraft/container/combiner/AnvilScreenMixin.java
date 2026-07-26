package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.IScreenType;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin implements IScreenType {
    @Override
    public String yyzsbackpack$getScreenType() {
        return "AnvilScreen";
    }
}
