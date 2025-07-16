package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.config.BackpackConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Backpack {
    public static final String MOD_ID = "yyzsbackpack";
    private static BackpackConfig config;

    public static void init() {
        config = BackpackConfig.loadConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
    }

    public static BackpackConfig getConfig() {
        return config;
    }
}
