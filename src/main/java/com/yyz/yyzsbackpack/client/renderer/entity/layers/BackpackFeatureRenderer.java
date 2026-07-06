package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState backpackRenderState = new ItemStackRenderState();
    private final Minecraft minecraft =  Minecraft.getInstance();

    public BackpackFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent,ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords,
                       AvatarRenderState state, float yRot, float xRot) {
        ItemStack backpack = BackpackSlotHelper.getSyncedBackpack(state.id);
        renderBackpack(poseStack, submitNodeCollector, lightCoords, state,backpack);
    }

    private void renderBackpack(PoseStack matrices, SubmitNodeCollector submitNodeCollector,
                                int light, AvatarRenderState state, ItemStack stack) {

        if(minecraft.player == null) return;
        if (!(stack.getItem() instanceof BackpackItem)) return;

        // 更新物品渲染状态
        this.itemModelResolver.updateForLiving(this.backpackRenderState, stack, ItemDisplayContext.NONE, minecraft.player);
        if (this.backpackRenderState.isEmpty()) return;

        matrices.pushPose();
        // 对齐身体模型
        this.getParentModel().body.translateAndRotate(matrices);
        matrices.scale(0.8F, 0.8F, 0.8F);

        // 放置在身体后方
        matrices.translate(0.0F, 7.0F / 16.0F, 6.0F / 16.0F);
        matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // 提交渲染
        this.backpackRenderState.submit(
                matrices,
                submitNodeCollector,
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );

        matrices.popPose();
    }
}