package com.yyz.yyzsbackpack.api;

public interface IBackpackScroll {
    int getScrollOffset();
    void setScrollOffset(int offset);
    int getMaxScrollOffset();
    void setMaxScrollOffset(int max);
}