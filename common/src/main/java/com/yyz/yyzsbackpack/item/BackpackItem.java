package com.yyz.yyzsbackpack.item;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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