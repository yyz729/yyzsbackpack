package com.yyz.yyzsbackpack.item;

import net.minecraft.item.Item;

public class BackpackItem extends Item {
    private final Backpack backpackType;

    public BackpackItem(Backpack backpack, Settings settings) {
        super(settings);
        this.backpackType = backpack;
    }

    public Backpack getBackpackType() {
        return backpackType;
    }
}