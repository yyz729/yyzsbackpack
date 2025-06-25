package com.yyz.yyzsbackpack.neoforge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BackpackPlatformImpl {

    public static DataComponentType<List<ItemStack>> getBackpackItemsComponent() {
        // Just throw an error, the content should get replaced at runtime.
        return BackpackNeoForge.BACKPACK_ITEMS_COMPONENT.get();
    }



}
