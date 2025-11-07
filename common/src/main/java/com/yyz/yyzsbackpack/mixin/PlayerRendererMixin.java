package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackRenderState;
import com.yyz.yyzsbackpack.client.DetailedBackpackFeatureRenderer;
import com.yyz.yyzsbackpack.client.SimplifiedBackpackFeatureRenderer;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {


    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void injectCustomFeature(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        AvatarRenderer renderer = (AvatarRenderer) (Object) this;
        super.addLayer(new DetailedBackpackFeatureRenderer(renderer));
        super.addLayer(new SimplifiedBackpackFeatureRenderer(renderer));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("RETURN")
    )
    private void injectExtractRenderState(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f, CallbackInfo ci) {
        if (avatar != null) {
            ((BackpackRenderState) avatarRenderState).yyzsbackpack$setShouldRender(shouldRender(avatar));
            ((BackpackRenderState) avatarRenderState).yyzsbackpack$setDyeColor(DyedItemColor.getOrDefault(BackpackPlatform.getEquippedL(avatar), -6265536));
            ((BackpackRenderState) avatarRenderState).yyzsbackpack$setDetailedOverlayTexture(getOverlayTexture(avatar));
        }
    }

    @Unique
    private static boolean shouldRender(Avatar player) {
        return BackpackPlatform.getEquippedL(player).getItem() instanceof BackpackItem && Backpack.getConfig().render_backpack_model && Backpack.getConfig().backpack_model_style.equals("detailed");
    }

    @Unique
    private ResourceLocation getOverlayTexture(LivingEntity player) {
        if(BackpackPlatform.getEquippedL(player).getItem() instanceof BackpackItem backpackItem){
            return backpackItem.getBackpackType().getModelTexture();
        }
        return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/gold_backpack.png");
    }
}
