package com.yyz.yyzsbackpack.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackRenderState;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.DyedItemColor;


public class DetailedBackpackFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ModelPart backpack;
    private final ModelPart backpack_overlay;


    public DetailedBackpackFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent) {
        super(renderLayerParent);
        this.backpack = createBackpackModel();
        this.backpack_overlay =  createBackpackOverlayModel();
    }


    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState entityRenderState, float f, float g) {
        Avatar player = ((BackpackRenderState)entityRenderState).getAbstractClientPlayer();
        if (shouldRender(player)) {

            int j = DyedItemColor.getOrDefault(BackpackPlatform.getEquippedL(player), -6265536);

            poseStack.pushPose();
            this.getParentModel().body.translateAndRotate(poseStack);
            poseStack.scale(0.8f, 0.8f, 0.8f);
            submitNodeCollector.submitModelPart(backpack,poseStack,RenderType.entitySolid(getTexture()),i, OverlayTexture.NO_OVERLAY,(TextureAtlasSprite)null,j,null);
            submitNodeCollector.submitModelPart(backpack_overlay,poseStack,RenderType.entitySolid(getOverlayTexture(player)),i, OverlayTexture.NO_OVERLAY,(TextureAtlasSprite)null);

            poseStack.popPose();
        }

    }


    private ModelPart createBackpackModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition backpack = partdefinition.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(38, 27).addBox(-5.0F, -14.5F, -4.0F, 10.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -14.0F, -5.0F, 12.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 31).addBox(-4.0F, -12.0F, 0.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-6.9F, -7.0F, -5.0F, 1.0F, 7.0F, 5.0F, new CubeDeformation(-0.1F))
                .texOffs(34, 0).addBox(-7.5F, -6.0F, -4.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-6.7F, -13.0F, -4.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(34, 8).addBox(5.7F, -13.0F, -4.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(5.9F, -7.0F, -5.0F, 1.0F, 7.0F, 5.0F, new CubeDeformation(-0.1F))
                .texOffs(34, 8).addBox(6.5F, -6.0F, -4.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 23).addBox(-5.0F, -0.5F, -4.0F, 10.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 8.0F));

        return meshdefinition.getRoot().bake(64, 64).getChild("backpack");
    }

    private ModelPart createBackpackOverlayModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition backpack_overlay = partdefinition.addOrReplaceChild("backpack_overlay", CubeListBuilder.create().texOffs(30, 31).addBox(-2.0F, -18.0F, 7.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(-0.3F))
                .texOffs(42, 32).addBox(0.0F, -13.5F, 8.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return meshdefinition.getRoot().bake(64, 64).getChild("backpack_overlay");
    }



    private boolean shouldRender(Avatar player) {
        return BackpackPlatform.getEquippedL(player).getItem() instanceof BackpackItem && Backpack.getConfig().render_backpack_model && Backpack.getConfig().backpack_model_style.equals("detailed");
    }
    private ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/backpack.png");
    }
    private ResourceLocation getOverlayTexture(LivingEntity player) {
        if(BackpackPlatform.getEquippedL(player).getItem() instanceof BackpackItem backpackItem){
            return backpackItem.getBackpackType().getModelTexture();
        }
        return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/gold_backpack.png");
    }


}
