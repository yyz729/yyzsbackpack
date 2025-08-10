package com.yyz.yyzsbackpack.item;

import net.minecraft.resources.ResourceLocation;

public interface BackpackMaterial {
    int getSize();
    int getColumns();
    int getRows();
    String getType();

    ResourceLocation getGuiTexture();

    ResourceLocation getModelTexture();
}
