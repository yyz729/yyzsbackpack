package com.yyz.yyzsbackpack.item;


import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class BackpackItem extends Item{

    private final String type;

    public BackpackItem(String type, Properties settings) {
        super(settings.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        this.type = type;
    }

    public BackpackData getData() {
        return BackpackDataLoader.getData(type);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }
}