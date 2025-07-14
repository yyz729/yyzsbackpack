package com.yyz.yyzsbackpack.base;

import net.minecraft.client.player.AbstractClientPlayer;

public interface BackpackRenderState {
    void setAbstractClientPlayer(AbstractClientPlayer player);

    AbstractClientPlayer getAbstractClientPlayer();
}
