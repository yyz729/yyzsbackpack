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
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Random;


public class BackpackFeatureRenderer extends RenderLayer<PlayerRenderState, PlayerModel> {

    private final ModelPart backpack;
    private final Random random = new Random();

    public BackpackFeatureRenderer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent) {
        super(renderLayerParent);
        this.backpack = createBackpackModel();
    }


    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState entityRenderState, float f, float g) {
        AbstractClientPlayer player = ((BackpackRenderState)entityRenderState).getAbstractClientPlayer();
        if (shouldRender(player)) {
            renderShield(poseStack, multiBufferSource, player, i);
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
                .texOffs(38, 23).addBox(-5.0F, -0.5F, -4.0F, 10.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(30, 31).addBox(-2.0F, -8.0F, -1.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(-0.3F))
                .texOffs(42, 32).addBox(0.0F, -3.5F, 0.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 8.0F));


        return meshdefinition.getRoot().bake(64, 64).getChild("backpack");
    }



    private boolean shouldRender(Player player) {
        // 这里添加你的显示条件，比如检查物品或状态效果
        return BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem && Backpack.getConfig().render_backpack_model;
    }

    private ResourceLocation getTexture(Player player) {
        // 这里添加你的显示条件，比如检查物品或状态效果
        if(BackpackPlatform.getEquipped(player).getItem() instanceof BackpackItem backpackItem){
            return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/"+backpackItem.getBackpackType().getType()+"_backpack.png");

        }
        return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/gold_backpack.png");

    }

    private void renderShield(PoseStack matrices, MultiBufferSource vertexConsumers, Player player, int light) {
        matrices.pushPose();
        this.getParentModel().body.translateAndRotate(matrices);
        matrices.scale(0.8f,0.8f,0.8f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entitySolid(getTexture(player)));
        this.backpack.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY);

        matrices.popPose();
    }


}
