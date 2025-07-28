package com.yyz.yyzsbackpack.item;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class BackpackItem extends Item {
    private final BackpackMaterial backpackMaterialsType;

    public BackpackItem(BackpackMaterial backpackMaterials, Properties settings) {
        super(settings);
        this.backpackMaterialsType = backpackMaterials;

    }

    public BackpackMaterial getBackpackType() {
        return backpackMaterialsType;
    }
}