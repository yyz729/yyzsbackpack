package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.config.BackpackConfig;

import java.io.File;

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
