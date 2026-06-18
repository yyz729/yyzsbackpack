package com.yyz.yyzsbackpack.item;


import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import net.minecraft.world.item.Item;

public class BackpackItem extends Item {

    private final String type;

    public BackpackItem(String type, Properties settings) {
        super(settings);
        this.type = type;
    }

    public BackpackData getData() {
        return BackpackDataLoader.getData(type);
    }
}