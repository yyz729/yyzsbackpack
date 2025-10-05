package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.client.DetailedBackpackFeatureRenderer;
import com.yyz.yyzsbackpack.client.SimplifiedBackpackFeatureRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void injectCustomFeature(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        PlayerRenderer renderer = (PlayerRenderer) (Object) this;
        super.addLayer(new DetailedBackpackFeatureRenderer(renderer));
        super.addLayer(new SimplifiedBackpackFeatureRenderer(renderer));
    }
}
