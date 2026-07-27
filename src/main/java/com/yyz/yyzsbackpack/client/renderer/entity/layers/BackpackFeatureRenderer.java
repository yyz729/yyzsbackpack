package com.yyz.yyzsbackpack.client.renderer.entity.layers; // 可根据实际包路径调整

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class BackpackFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static Predicate<AbstractClientPlayer> renderCondition = player -> true;

    public static void setRenderCondition(Predicate<AbstractClientPlayer> condition) {
        renderCondition = condition != null ? condition : player -> true;
    }

    public BackpackFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                       AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // 配置开关
        if (!Backpack.getMainConfig().model) return;

        if (!renderCondition.test(entity)) return;

        // 通过实体 ID 获取背包
        ItemStack backpack = BackpackSlotHelper.getSyncedBackpack(entity.getId());
        if (!(backpack.getItem() instanceof BackpackItem)) return;

        //开始渲染
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.0F, 7.0F / 16.0F, 6.0F / 16.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // 使用ItemRenderer 渲染物品
        Minecraft.getInstance().getItemRenderer().renderStatic(
                backpack,
                ItemDisplayContext.NONE,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }
}