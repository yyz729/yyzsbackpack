package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BackpackFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BackpackFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context) {
        super(context);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int light,
                       AbstractClientPlayer entity, float f, float g, float h, float j, float k, float l) {
        if (shouldRender(entity)) {
            renderBackpack(poseStack, multiBufferSource, entity, light);
        }
    }

    private boolean shouldRender(Player player) {
        ItemStack stack = BackpackPlatform.getEquipped(player);
        return stack.getItem() instanceof BackpackItem && BackpackPlatform.getRender(player);
    }

    private void renderBackpack(PoseStack matrices, MultiBufferSource vertexConsumers, Player player, int light) {
        ItemStack stack = BackpackPlatform.getEquipped(player);
        if (!(stack.getItem() instanceof BackpackItem)) return;

        matrices.pushPose();
        // 对齐身体模型
        this.getParentModel().body.translateAndRotate(matrices);
        matrices.scale(0.8F, 0.8F, 0.8F);

        // 放置在身体后方（向上14像素，向后8像素）
        matrices.translate(0.0F, 7.0F / 16.0F, 6.0F / 16.0F);
        // 旋转180°使模型正面朝后（后背方向）
        matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // 使用物品当前模型渲染，不应用物品自身的显示变换（NONE）
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.NONE,
                light,
                OverlayTexture.NO_OVERLAY,
                matrices,
                vertexConsumers,
                player.level(),
                player.getId()
        );
        matrices.popPose();
    }
}