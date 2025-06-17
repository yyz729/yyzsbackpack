package com.yyz.yyzsbackpack.fabric;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BackpackPlatformImpl {

    public static DataComponentType<List<ItemStack>> getBackpackItemsComponent() {
        return BackpackFabric.BACKPACK_ITEMS_COMPONENT;
    }
}
