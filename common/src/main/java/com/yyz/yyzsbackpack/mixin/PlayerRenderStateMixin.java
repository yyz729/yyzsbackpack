package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.base.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class PlayerRenderStateMixin implements BackpackRenderState {
    @Unique private int yyzsbackpack$dyeColor = -6265536;
    @Unique private boolean yyzsbackpack$shouldRender;
    @Unique private ResourceLocation yyzsbackpack$detailedOverlayTexture;

    @Override
    public boolean yyzsbackpack$shouldRender() {
        return this.yyzsbackpack$shouldRender;
    }

    @Override
    public void yyzsbackpack$setShouldRender(boolean value) {
        this.yyzsbackpack$shouldRender = value;
    }

    @Override
    public int yyzsbackpack$getDyeColor() {
        return this.yyzsbackpack$dyeColor;
    }

    @Override
    public void yyzsbackpack$setDyeColor(int color) {
        this.yyzsbackpack$dyeColor = color;
    }

    @Override
    public ResourceLocation yyzsbackpack$getDetailedOverlayTexture() {
        return this.yyzsbackpack$detailedOverlayTexture;
    }

    @Override
    public void yyzsbackpack$setDetailedOverlayTexture(ResourceLocation location) {
        this.yyzsbackpack$detailedOverlayTexture = location;
    }
}
