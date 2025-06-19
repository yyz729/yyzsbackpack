package com.yyz.yyzsbackpack.fabric;


import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConditionalMixinHelperImpl {
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

}