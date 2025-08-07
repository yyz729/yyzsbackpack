package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackRenderState;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.DyedItemColor;

public class SimplifiedBackpackFeatureRenderer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ModelPart backpack;

    public SimplifiedBackpackFeatureRenderer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent) {
        super(renderLayerParent);
        this.backpack = createBodyLayer();
    }

    public static ModelPart createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leather_backpack = partdefinition.addOrReplaceChild("leather_backpack", CubeListBuilder.create().texOffs(0, 8).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(26, 8).mirror().addBox(-4.0F, -8.0F, 1.0F, 2.0F, 11.0F, 5.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(26, 8).mirror().addBox(2.0F, -8.0F, 1.0F, 2.0F, 11.0F, 5.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(4, 0).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.75F, 3.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition top = leather_backpack.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -1.0F, -4.0F, 9.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -7.5F, 0.5F));

        PartDefinition cube_r1 = top.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 29).addBox(-4.5F, -2.0F, 0.0F, 9.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 5.9829F, -3.7389F, 0.1309F, 0.0F, 0.0F));

        return meshdefinition.getRoot().bake(64, 64).getChild("leather_backpack");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState entityRenderState, float f, float g) {
        AbstractClientPlayer player = ((BackpackRenderState)entityRenderState).getAbstractClientPlayer();
        if (shouldRender(player)) {
            renderBackpack(poseStack, multiBufferSource, player, i);
        }
    }

    private void renderBackpack(PoseStack matrices, MultiBufferSource vertexConsumers, Player player, int light) {

        if(BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem) {
            int j = DyedItemColor.getOrDefault(BackpackPlatform.getEquipped(player), -6265536);
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/leather_backpack1.png")));
            this.backpack.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY,j);

            matrices.popPose();
        }
    }
    private boolean shouldRender(Player player) {
        return BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem && Backpack.getConfig().render_backpack_model  && Backpack.getConfig().backpack_model_style.equals("simplified");
    }

}
