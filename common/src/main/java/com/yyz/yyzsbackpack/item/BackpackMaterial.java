package com.yyz.yyzsbackpack.item;

import net.minecraft.resources.ResourceLocation;

public interface BackpackMaterial {
    int getSize();
    int getColumns();
    String getType();

    ResourceLocation getGuiTexture();

    ResourceLocation getModelTexture();
}
