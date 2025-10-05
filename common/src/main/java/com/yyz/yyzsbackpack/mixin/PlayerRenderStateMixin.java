package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.base.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class PlayerRenderStateMixin implements BackpackRenderState {
    @Unique
    private Avatar player;
    @Override
    public void setAbstractClientPlayer(Avatar player) {
        this.player = player;
    }

    @Override
    public Avatar getAbstractClientPlayer() {
        return this.player;
    }
}
