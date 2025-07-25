package com.yyz.yyzsbackpack.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
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
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    public static ItemStack getEquipped(Player player) {
        return BackpackFabric.getEquipped(player);
    }
    public static Container getContainer(Player player) {
        return BackpackFabric.getContainer(player);
    }
    public static int getIndex(Player player){
        return BackpackFabric.getIndex(player);
    }
    public static boolean getEmptyRule(Player player) {
        return true;
    }
}
