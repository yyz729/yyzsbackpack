package com.yyz.yyzsbackpack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public class BackpackHelper {
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

    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean getEmptyRule(Player player) {
        throw new AssertionError();
    }
}
