package com.yyz.yyzsbackpack;


import com.mojang.brigadier.CommandDispatcher;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;

import java.io.File;

public class Backpack{

    public static final String MOD_ID = "yyzsbackpack";


    private static BackpackConfig config;

    public static void init() {
        config = BackpackConfig.loadConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
    }
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        BackpackCommand.register(dispatcher);
    }
    public static BackpackConfig getConfig() {
        return config;
    }
}
