package com.yyz.yyzsbackpack.forge;


import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class BackpackHelperImpl {
    public static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }
    public static ItemStack getEquipped(Player player) {
        return BackpackForge.getEquipped(player);
    }
    public static Container getContainer(Player player) {
        return BackpackForge.getContainer(player);
    }
    public static int getIndex(Player player){
        return BackpackForge.getIndex(player);
    }

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

}