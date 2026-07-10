package com.yyz.yyzsbackpack.api;

public interface IBackpackScroll {

    int yyzsbackpack$getSegmentCount();
    int yyzsbackpack$getSegmentScrollOffset(int index);
    void yyzsbackpack$setSegmentScrollOffset(int index, int offset);
    int yyzsbackpack$getSegmentMaxScrollOffset(int index);
    void yyzsbackpack$setSegmentMaxScrollOffset(int index, int max);
}