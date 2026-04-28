package com.yyz.yyzsbackpack;


import com.mojang.brigadier.CommandDispatcher;
import com.yyz.yyzsbackpack.command.BackpackCommand;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.data.BackpackMaterialManager;
import com.yyz.yyzsbackpack.util.BackpackSorter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.effect.MobEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Backpack{

    public static final String MOD_ID = "yyzsbackpack";
    public static final Logger LOGGER = LoggerFactory.getLogger("yyzsbackpack");

    private static BackpackConfig config;

    public static void init() {
        BackpackMaterialManager.loadMaterials();
        BackpackSorter.loadDefaultCustomSort();
        config = BackpackConfig.loadConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));

    }
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        BackpackCommand.register(dispatcher);
    }
    public static BackpackConfig getConfig() {
        return config;
    }
}
