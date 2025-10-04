package com.yyz.yyzsbackpack.item;


import com.yyz.yyzsbackpack.data.BackpackMaterialManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BackpackItem extends Item implements DyeableLeatherItem{
    private final String string;

    public BackpackItem(String iron, Properties settings) {
        super(settings);
        this.string = iron;

    }

    public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 10511680;
    }

    public BackpackMaterial getBackpackType() {
        return BackpackMaterialManager.getMaterial(string);
    }


}