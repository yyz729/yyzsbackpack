package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SimplifiedBackpackFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart backpack;
    private final ModelPart backpack_overlay;

    public SimplifiedBackpackFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context) {
        super(context);
        this.backpack = createBackpackModel();
        this.backpack_overlay =  createBackpackOverlayModel();
    }

    private ModelPart createBackpackModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leather_backpack = partdefinition.addOrReplaceChild("leather_backpack", CubeListBuilder.create().texOffs(0, 8).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(26, 8).mirror().addBox(-4.0F, -8.0F, 1.0F, 2.0F, 11.0F, 5.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(26, 8).mirror().addBox(2.0F, -8.0F, 1.0F, 2.0F, 11.0F, 5.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(4, 0).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.75F, 4.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition top = leather_backpack.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -1.0F, -4.0F, 9.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -7.5F, 0.5F));

        return meshdefinition.getRoot().bake(64, 64).getChild("leather_backpack");
    }

    private ModelPart createBackpackOverlayModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leather_backpack_overlay = partdefinition.addOrReplaceChild("leather_backpack_overlay", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r2 = leather_backpack_overlay.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 29).addBox(-4.5F, -2.0F, 0.0F, 9.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.75F, 7.25F, 0.1309F, 3.1416F, 0.0F));

        return meshdefinition.getRoot().bake(64, 64).getChild("leather_backpack_overlay");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer entity, float f, float g, float h, float j, float k, float l) {
        if (shouldRender(entity)) {
            renderBackpack(poseStack, multiBufferSource, entity, i);
        }
    }

    private void renderBackpack(PoseStack matrices, MultiBufferSource vertexConsumers, Player player, int light) {

        if(BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem backpackItem) {
            int j = backpackItem.getColor(BackpackPlatform.getEquipped(player));
            float red = (float)(j >> 16 & 255) / 255.0F;
            float green = (float)(j >> 8 & 255) / 255.0F;
            float blue = (float)(j & 255) / 255.0F;
            float alpha = (float)(j >> 24 & 255) / 255.0F;
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(new ResourceLocation(Backpack.MOD_ID, "textures/backpack/leather_backpack.png")));
            this.backpack.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0f);

            VertexConsumer overlay_vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(new ResourceLocation(Backpack.MOD_ID, "textures/backpack/leather_backpack_overlay.png")));
            this.backpack_overlay.render(matrices, overlay_vertexConsumer, light, OverlayTexture.NO_OVERLAY);

            matrices.popPose();
        }
    }
    private boolean shouldRender(Player player) {
        return BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem && Backpack.getConfig().render_backpack_model  && Backpack.getConfig().backpack_model_style.equals("simplified");
    }

}
