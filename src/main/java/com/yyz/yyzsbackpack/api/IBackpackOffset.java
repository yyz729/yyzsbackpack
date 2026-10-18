package com.yyz.yyzsbackpack.api;

/**
 * 由需要提供背包自定义界面偏移的屏幕实现。
 * 返回的偏移量会被加算到槽位 X Y 坐标和背景绘制 X Y坐标上。
 */
public interface IBackpackOffset {
    /**
     * 按分段返回 X 方向偏移。段 0 即第一个分段，
     * 页签/标题等不分段的元素统一取段 0。
     *
     * @param segmentIndex 分段索引
     * @return 该分段的额外 X 方向偏移
     */
    int yyzsbackpack$getBackpackOffsetX(int segmentIndex);

    /**
     * 按分段返回 Y 方向偏移。段 0 即第一个分段。
     */
    int yyzsbackpack$getBackpackOffsetY(int segmentIndex);
}
