package com.yyz.yyzsbackpack.base;

import net.minecraft.world.entity.Avatar;

public interface BackpackRenderState {
    void setAbstractClientPlayer(Avatar player);

    Avatar getAbstractClientPlayer();
}
