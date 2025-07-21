package com.yyz.yyzsbackpack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;

public class BackpackPlatform {

    @ExpectPlatform
    public static DataComponentType<List<ItemStack>> getBackpackItemsComponent() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
    @ExpectPlatform
    public static boolean isFabric() {
        throw new AssertionError();
    }

}
