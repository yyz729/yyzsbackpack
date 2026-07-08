package com.yyz.yyzsbackpack.api;

/**
 * 由需要背包切换功能的屏幕实现。
 * 每个屏幕实例维护自己的可见性状态。
 */
public interface IBackpackToggle {
    boolean yyzsbackpack$isBackpackVisible();
    void yyzsbackpack$setBackpackVisible(boolean visible);
    
    default void yyzsbackpack$toggleBackpackVisible() {
        yyzsbackpack$setBackpackVisible(!yyzsbackpack$isBackpackVisible());
    }
}