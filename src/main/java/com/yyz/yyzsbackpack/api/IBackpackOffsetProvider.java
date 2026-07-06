package com.yyz.yyzsbackpack.api;

/**
 * 由需要提供背包自定义界面偏移的屏幕实现。
 * 返回的偏移量会被加算到槽位 X Y 坐标和背景绘制 X Y坐标上。
 */
public interface IBackpackOffsetProvider {
    /**
     * @return 额外的 X 方向偏移
     */
    int yyzsbackpack$getBackpackOffsetX();
    /**
     * @return 额外的 Y 方向偏移
     */
    int yyzsbackpack$getBackpackOffsetY();
}