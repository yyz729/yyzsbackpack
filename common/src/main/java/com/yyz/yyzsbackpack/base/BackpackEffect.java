package com.yyz.yyzsbackpack.base;

public class BackpackEffect {
    public String effectType = "none"; // 效果类型标识符
    public int amplifier = 0;         // 效果数值

    public BackpackEffect() {} // Gson需要无参构造函数

    public BackpackEffect(String effectType, int amplifier) {
        this.effectType = effectType;
        this.amplifier = amplifier;
    }
}
