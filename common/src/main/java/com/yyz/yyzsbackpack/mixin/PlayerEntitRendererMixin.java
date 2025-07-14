package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.client.BackpackFeatureRenderer;
import com.yyz.yyzsbackpack.base.BackpackRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntitRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {


    public PlayerEntitRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void injectCustomFeature(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        PlayerRenderer renderer = (PlayerRenderer) (Object) this;
        super.addLayer(new BackpackFeatureRenderer(renderer));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("RETURN")
    )
    private void injectExtractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        ((BackpackRenderState)playerRenderState).setAbstractClientPlayer(abstractClientPlayer);
    }
}
