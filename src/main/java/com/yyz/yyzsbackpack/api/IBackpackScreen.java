package com.yyz.yyzsbackpack.api;

public interface IBackpackScreen {
    String yyzsbackpack$getScreenType();

    /** 返回 false 表示该界面禁用 Alt+左键快速移动 */
    default boolean yyzsbackpack$allowQuickMove() {
        return true;
    }
}