package com.yyz.yyzsbackpack.item;

public enum Backpack {
    WOOLEN(1),
    STONE(2),
    IRON(3),
    GOLD(4),
    DIAMOND(5),
    NETHERITE(6);

    private final int columns;

    Backpack(int columns) {
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }
}