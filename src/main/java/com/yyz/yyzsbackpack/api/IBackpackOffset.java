package com.yyz.yyzsbackpack.api;

/**
 * 由需要提供背包自定义界面偏移的屏幕实现。
 * 返回的偏移量会被加算到槽位 X Y 坐标和背景绘制 X Y坐标上。
 */
public interface IBackpackOffset {
    /**
     * @return 额外的 X 方向偏移
     */
    int yyzsbackpack$getBackpackOffsetX();
    /**
     * @return 额外的 Y 方向偏移
     */
    int yyzsbackpack$getBackpackOffsetY();

    /**
     * 按分段返回 X 方向偏移。默认与全局偏移一致；需要按分段区分
     * （例如配方书偏移的多段配置）的屏幕实现可重写此方法。
     *
     * @param segmentIndex 分段索引
     * @return 该分段的额外 X 方向偏移
     */
    default int yyzsbackpack$getBackpackOffsetX(int segmentIndex) {
        return yyzsbackpack$getBackpackOffsetX();
    }

    /**
     * 按分段返回 Y 方向偏移。默认与全局偏移一致。
     */
    default int yyzsbackpack$getBackpackOffsetY(int segmentIndex) {
        return yyzsbackpack$getBackpackOffsetY();
    }
}