package com.yyz.yyzsbackpack.neoforge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

public class BackpackPlatformImpl {

    public static DataComponentType<List<ItemStack>> getBackpackItemsComponent() {
        // Just throw an error, the content should get replaced at runtime.
        return BackpackNeoForge.BACKPACK_ITEMS_COMPONENT.get();
    }

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
    public static boolean isFabric() {
        return false;
    }

    public static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static ItemStack getEquipped(Player player) {
        return BackpackNeoForge.getEquipped(player);
    }

    public static Container getContainer(Player player) {
        return BackpackNeoForge.getContainer(player);
    }

    public static int getIndex(Player player){
        return BackpackNeoForge.getIndex(player);
    }

    public static boolean getEmptyRule(Player player) {
        return true;
    }
}
