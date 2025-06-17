package com.yyz.yyzsbackpack.item;


import net.minecraft.world.item.Item;

public class BackpackItem extends Item {
    private final BackpackMaterial backpackMaterialType;

    public BackpackItem(BackpackMaterial backpackMaterial, Properties settings) {
        super(settings);
        this.backpackMaterialType = backpackMaterial;
    }

    public BackpackMaterial getBackpackType() {
        return backpackMaterialType;
    }
}