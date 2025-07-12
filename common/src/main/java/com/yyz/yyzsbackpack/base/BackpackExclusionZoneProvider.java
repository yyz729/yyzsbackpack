package com.yyz.yyzsbackpack.base;

import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public interface BackpackExclusionZoneProvider {
    /**
     * 获取背包的排除区域列表
     */
    List<Rect2i> getBackpackExclusionZones();
}