package com.yyz.yyzsbackpack.api.enums;

public enum MoveMode {
    ALL((byte)0),
    MATCHING((byte)1),
    FILL_MATCHING((byte)2),
    ONE_EACH((byte)3);

    private final byte id;

    MoveMode(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static MoveMode fromId(byte id) {
        for (MoveMode mode : values()) {
            if (mode.id == id) return mode;
        }
        return MATCHING;
    }
}