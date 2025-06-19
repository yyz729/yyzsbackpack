package com.yyz.yyzsbackpack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ConditionalMixinHelper {
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static ItemStack getEquipped(Player player) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static Container getContainer(Player player) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static int getIndex(Player player){
        throw new AssertionError();
    }

}
