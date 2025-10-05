package com.yyz.yyzsbackpack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getEquipped(Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getEquippedL(LivingEntity player) {
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
    public static boolean getEmptyRule(Player player) {
        throw new AssertionError();
    }
}
