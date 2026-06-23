package com.yyz.yyzsbackpack.api;

import com.mojang.serialization.Codec;

public enum LayoutOrder {
    DEFAULT,  CUSTOM;

    public static final Codec<LayoutOrder> CODEC = Codec.STRING.xmap(LayoutOrder::valueOf, LayoutOrder::name);
}
