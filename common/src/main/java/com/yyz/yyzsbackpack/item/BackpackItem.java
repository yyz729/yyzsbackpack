package com.yyz.yyzsbackpack.item;


import com.yyz.yyzsbackpack.data.BackpackMaterialManager;
import net.minecraft.world.item.Item;

public class BackpackItem extends Item {
    private final String string;

    public BackpackItem(String iron, Properties settings) {
        super(settings);
        this.string = iron;

    }


    public BackpackMaterial getBackpackType() {
//        return backpackMaterialsType;
        return BackpackMaterialManager.getMaterial(string);
    }
}