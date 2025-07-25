package com.yyz.yyzsbackpack.item;

public enum BackpackMaterial {
//    WOODEN("wooden",1),
//    STONE("stone",2),
    IRON("iron",2),
    GOLD("gold",4),
    DIAMOND("diamond",6),
    NETHERITE("netherite",6);

    private final String type;
    private final int columns;

    BackpackMaterial(String type,int columns) {
        this.type = type;
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }

    public String getType() {
        return type;
    }
}