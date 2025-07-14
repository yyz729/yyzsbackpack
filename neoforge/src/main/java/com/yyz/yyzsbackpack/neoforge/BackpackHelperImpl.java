package com.yyz.yyzsbackpack.neoforge;


import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLLoader;

public class BackpackHelperImpl {

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
