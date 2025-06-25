package com.yyz.yyzsbackpack.api;

import me.shedaniel.math.Rectangle;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public interface BackpackExclusionZoneProvider {
    /**
     * 获取背包的排除区域列表
     */
    List<Rect2i> getBackpackExclusionZones();
}