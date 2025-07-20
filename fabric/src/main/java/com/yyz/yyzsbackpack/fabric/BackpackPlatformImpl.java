package com.yyz.yyzsbackpack.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;

public class BackpackPlatformImpl {

    public static DataComponentType<List<ItemStack>> getBackpackItemsComponent() {
        return BackpackFabric.BACKPACK_ITEMS_COMPONENT;
    }

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    public static boolean isFabric() {
        return true;
    }
}
