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
    void setLeftPos(int leftPos);

    @Accessor("leftPos")
    int getLeftPos();

    @Accessor("topPos")
    @Mutable
    void setTopPos(int topPos);

    @Accessor("topPos")
    int getTopPos();

    @Accessor("imageWidth")
    int getImageWidth();

    @Accessor("imageHeight")
    int getImageHeight();
}