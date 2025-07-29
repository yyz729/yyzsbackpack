package com.yyz.yyzsbackpack.item;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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