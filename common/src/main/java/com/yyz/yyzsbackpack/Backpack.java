package com.yyz.yyzsbackpack;


import com.yyz.yyzsbackpack.config.BackpackConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class Backpack{

    public static final String MOD_ID = "yyzsbackpack";


    private static BackpackConfig config;

    public static void init() {
        config = BackpackConfig.loadConfig(new File(BackpackHelper.getConfigDirectory() + "/yyzsbackpack.json"));
    }

    public static BackpackConfig getConfig() {
        return config;
    }
}
