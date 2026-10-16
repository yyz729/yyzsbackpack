package com.yyz.yyzsbackpack.mixin.minecraft.container.combiner;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin implements IBackpackScreen {
    @Override
    public String yyzsbackpack$getScreenType() {
        return "AnvilScreen";
    }
}
