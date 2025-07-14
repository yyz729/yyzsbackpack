package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.base.BackpackRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements BackpackRenderState {
    @Unique
    private AbstractClientPlayer player;
    @Override
    public void setAbstractClientPlayer(AbstractClientPlayer player) {
        this.player = player;
    }

    @Override
    public AbstractClientPlayer getAbstractClientPlayer() {
        return this.player;
    }
}
