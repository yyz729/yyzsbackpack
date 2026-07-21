package com.yyz.yyzsbackpack.mixin.minecraft.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ScreenAccessor<T extends AbstractContainerMenu> {

    @Accessor("leftPos")
    @Mutable
    void yyzsbackpack_setLeftPos(int leftPos);

    @Accessor("leftPos")
    int yyzsbackpack_getLeftPos();

    @Accessor("topPos")
    @Mutable
    void yyzsbackpack_setTopPos(int topPos);

    @Accessor("topPos")
    int yyzsbackpack_getTopPos();

    @Accessor("imageWidth")
    int yyzsbackpack_getImageWidth();

    @Accessor("imageHeight")
    int yyzsbackpack_getImageHeight();
}