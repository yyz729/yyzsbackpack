package com.yyz.yyzsbackpack.item;


import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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