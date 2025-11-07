package com.yyz.yyzsbackpack.base;

import net.minecraft.resources.ResourceLocation;

public interface BackpackRenderState {
    boolean yyzsbackpack$shouldRender();
    void yyzsbackpack$setShouldRender(boolean value);

    int yyzsbackpack$getDyeColor();
    void yyzsbackpack$setDyeColor(int color);

    ResourceLocation yyzsbackpack$getDetailedOverlayTexture();
    void yyzsbackpack$setDetailedOverlayTexture(ResourceLocation location);
}
